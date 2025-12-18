package com.gscorp.dv1.attendance.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gscorp.dv1.attendance.infrastructure.AttendancePunch;
import com.gscorp.dv1.attendance.infrastructure.AttendancePunchProjection;
import com.gscorp.dv1.attendance.infrastructure.AttendancePunchRepo;
import com.gscorp.dv1.attendance.web.dto.AttendancePunchDto;
import com.gscorp.dv1.attendance.web.dto.AttendancePunchPointDto;
import com.gscorp.dv1.attendance.web.dto.CreateAttendancePunchRequest;
import com.gscorp.dv1.attendance.web.dto.HourlyCountDto;
import com.gscorp.dv1.components.ZoneResolver;
import com.gscorp.dv1.components.dto.ZoneResolutionResult;
import com.gscorp.dv1.employees.application.EmployeeService;
import com.gscorp.dv1.employees.web.dto.EmployeeSelectDto;
import com.gscorp.dv1.sites.application.SiteService;
import com.gscorp.dv1.sites.web.dto.SiteSelectDto;
import com.gscorp.dv1.users.application.UserService;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl implements AttendanceService {

  private final AttendancePunchRepo repo;
  private final SiteService siteService;
  private final UserService userService;
  private final EmployeeService employeeService;
  private final ZoneResolver zoneResolver;

  private static final double MAX_DIST_METERS = 250.0;
  private static final DateTimeFormatter TS_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

  /** COMANDO: registrar marcación IN/OUT alternada usando el site más cercano */
  @Override
  @Transactional
  public AttendancePunchDto createPunch(
      CreateAttendancePunchRequest req, Long userId
    ) {

      // anti doble-click: buscar última marcación del usuario
      Optional<AttendancePunch> lastOpt = repo.findFirstByUserIdOrderByTsDesc(userId);
      AttendancePunch last = lastOpt.orElse(null);

      //Resolver zona para ahora()
      ZoneResolutionResult zoneResult = zoneResolver.
                                          resolveZone(userId, req.getClientTimezone());
      ZoneId zone = zoneResult.zoneId();
      var now  = OffsetDateTime.now(zone);

      // determinar lat/lon/accuracy desde request y validar que existan
      Double lat = req.getLat();
      Double lon = req.getLon();
      Double acc = req.getAccuracy();

      if (lat == null || lon == null) {
        throw new IllegalArgumentException("lat/lon son obligatorios para crear una marcación");
      }

      // Busca el site más cercano a la marcación
      SiteSelectDto nearestSite = siteService.findNearestSite(userId, lat, lon);
      if (nearestSite == null)
                throw new IllegalStateException("No hay sitios registrados");

      double siteLat = nearestSite.lat();
      double siteLon = nearestSite.lon();

      // Obtener empleado asociado al userId
      EmployeeSelectDto employee = employeeService.findEmployeeByUserId(userId);
      if (employee == null) {
        throw new IllegalStateException("El usuario no tiene un empleado asociado");
      }

      String ip = req.getIp();
      String ua = req.getDeviceInfo();

      double dist = siteService.haversineMeters(lat, lon, siteLat, siteLon);
      boolean ok  = dist <= MAX_DIST_METERS;

      String nextAction =
              (last == null || "OUT".equalsIgnoreCase(last.getAction())) ? "IN" : "OUT";

      AttendancePunch entity = AttendancePunch.builder()
          .siteId(nearestSite.id())
          .employeeId(employee.id())
          .userId(userId)
          .clientTs(req.getClientTs())
          .lat(lat)
          .lon(lon)
          .accuracyM(acc)
          .action(nextAction)
          .locationOk(ok)
          .distanceM(dist)
          .ip(ip)
          .deviceInfo(ua)
          .clientTimezone(req.getClientTimezone())
          .build();

      // Persistir entidad y manejar posibles violaciones de integridad/concurrencia
      AttendancePunch persisted;
      try {
          persisted = repo.save(entity);
          // flush opcional si quieres asegurar que constraints sean validados ahora: repo.flush();
      } catch (DataIntegrityViolationException ex) {
          // Capturar race-conditions (por ejemplo si employee fue asociado por otro hilo) y volver a informar
          throw new IllegalStateException("No se pudo crear la marcación por conflicto de integridad", ex);
      }

      //Formatear ts para la respuesta (user ts persistido para consistencia)
      OffsetDateTime ts = persisted.getTs() != null ? persisted.getTs() : now;
      String tsFormatted = ts.format(TS_FMT);

      AttendancePunchDto response  = new AttendancePunchDto(
          persisted.getId(),
          persisted.getUserId(),
          employee.id(),
          employee.name(),
          employee.fatherSurname(),
          persisted.getSiteId(),
          nearestSite.name(),
          ts,
          persisted.getLat(),
          persisted.getLon(),
          persisted.getAccuracyM(),
          persisted.getAction(),
          persisted.getLocationOk(),
          persisted.getDistanceM(),
          persisted.getDeviceInfo(),
          persisted.getIp(),
          persisted.getTimezoneSource(),
          persisted.getCreatedAt(),
          persisted.getUpdatedAt(),
          tsFormatted
      );

    return response;
  }



  /** ÚLTIMA marcación del usuario */
  @Transactional(readOnly = true)
  public Optional<AttendancePunch> lastPunch(Long userId) {
    return repo.findFirstByUserIdOrderByTsDesc(userId);
  }


  /** SERIE por usuario y rango (opcional: acción "IN"/"OUT") */
  @Transactional(readOnly = true)
  public List<AttendancePunchRepo.DayCount> seriesByUser(Long userId, LocalDate from, LocalDate to, String action) {
    return repo.countByDay(from, to, action, userId);
  }


  @Override
  @Transactional(readOnly = true)
  public long countByClientIdAndDate(Long clientId, LocalDate date) {
    var fromTs = date.atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime();
    var toTs   = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).minusNanos(1).toOffsetDateTime();
    return repo.countByClientIdAndTsBetween(clientId, fromTs, toTs);
  }


  @Override
  @Transactional(readOnly = true)
  public List<HourlyCountDto> getHourlyCounts(LocalDate date, String tz, String action, Long userId) {
    // Normalizar action
    String normalizedAction = normalizeAction(action);

    // Resolver ZoneId con fallback a UTC
    ZoneId zone;
    try {
      zone = (tz == null || tz.isBlank()) ? ZoneId.of("UTC") : ZoneId.of(tz);
    } catch (Exception ex) {
      zone = ZoneId.of("UTC");
    }

    // Calcular ventana [from, to) en la zona solicitada
    ZonedDateTime startZ = date.atStartOfDay(zone);
    ZonedDateTime endZ = startZ.plusDays(1);
    OffsetDateTime from = startZ.toOffsetDateTime();
    OffsetDateTime to = endZ.toOffsetDateTime();

    // Llamar al repo (consulta nativa que agrupa por hora dentro del rango)
    var rows = repo.findHourlyCountsForRange(from, to, tz, normalizedAction, userId);

    // Mapear proyección a DTO
    return rows.stream()
        .map(r -> new HourlyCountDto(r.getHour(), r.getCnt() == null ? 0L : r.getCnt()))
        .toList();
  }

  //Helper
  private static String normalizeAction(String a) {
    if (a == null) return null;
    var t = a.trim();
    return t.isEmpty() ? null : t.toUpperCase();
  }


    @Override
    @Transactional(readOnly = true)
    public long countByClientIdsAndDate(List<Long> clientIds, LocalDate date, String action, String tz) {
        if (clientIds == null || clientIds.isEmpty()) return 0L;
        if (date == null) return 0L;
        ZoneId zone;
        try {
            zone = (tz == null || tz.isBlank()) ? ZoneId.systemDefault() : ZoneId.of(tz);
        } catch (DateTimeException ex) {
            zone = ZoneId.systemDefault();
        }

        ZonedDateTime startZdt = date.atStartOfDay(zone);
        ZonedDateTime endZdt = startZdt.plusDays(1);

        OffsetDateTime from = startZdt.toOffsetDateTime();
        OffsetDateTime to = endZdt.toOffsetDateTime();

        // llama al repo JPQL que hace JOIN client->project->site->attendance
        return repo.countByClientIdsAndTsBetweenAndAction(clientIds, from, to, action);
    }


    @Override
    @Transactional(readOnly = true)
    public List<AttendancePunchDto> findByUserAndDateBetween(
              Long userId,
              LocalDate fromDate,
              LocalDate toDate,
              String clientTz,
              Long siteId,
              Long projectId,
              String action) {

        List<Long> clientIds = userService.getClientIdsForUser(userId);
        if (clientIds == null || clientIds.isEmpty()) {
            log.debug("No clientIds for user {} -> returning zero series for {}..{}", userId, fromDate, toDate);
            return Collections.emptyList();
        }

        ZoneResolutionResult zr = zoneResolver.resolveZone(userId, clientTz);
        ZoneId zone = zr.zoneId(); // o zone() según tu record
        // intervalo [start, end)
        OffsetDateTime start = fromDate.atStartOfDay(zone).toOffsetDateTime();
        OffsetDateTime endExclusive = toDate.plusDays(1).atStartOfDay(zone).toOffsetDateTime();

        // llamar repo que espera OffsetDateTime límites
        List<AttendancePunchProjection> rows = repo
                                                .findByClientIdsAndDateBetween(
                                                    clientIds, start, endExclusive, siteId, projectId, action);

        // mapear proyection -> DTO final y formatear según zone
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        return rows.stream().map(p -> {
            // Determinar la zona para formateo: preferir la que venga en la proyección (visit created)
            ZoneId displayZone = zone;
            String tzFromRow = p.getClientTimezone();
            if (tzFromRow != null && !tzFromRow.isBlank()) {
                try {
                    displayZone = ZoneId.of(tzFromRow);
                } catch (DateTimeException ex) {
                    log.debug("Invalid clientTimezone '{}' in row id={} - using resolved zone {}"
                                                , tzFromRow, p.getId(), zone);
                    displayZone = zone;
                }
            }

            String formatted = null;
            OffsetDateTime visitOffset = p.getTs();
            if (visitOffset != null) {
                Instant instant = visitOffset.toInstant();
                ZonedDateTime local = instant.atZone(displayZone);
                formatted = local.format(fmt);
            }

            return new AttendancePunchDto(
                p.getId(),
                p.getUserId(),
                p.getEmployeeId(),
                p.getEmployeeName(),
                p.getEmployeeFatherSurname(),
                p.getSiteId(),
                p.getSiteName(),
                p.getTs(),
                p.getLat(),
                p.getLon(),
                p.getAccuracyM(),
                p.getAction(),
                p.getLocationOk(),
                p.getDistanceM(),
                p.getDeviceInfo(),
                p.getIp(),
                p.getTimezoneSource(),
                p.getCreatedAt(),
                p.getUpdatedAt(),
                formatted
            );
        }).collect(Collectors.toList());


    }


    @Override
    @Transactional(readOnly = true)
    public List<AttendancePunchPointDto> getAttendanceSeriesForUserByDates(
            Long userId,
            LocalDate fromDate,
            LocalDate toDate,
            ZoneId zone,
            String action,
            Long siteId,
            Long projectId
    ){
      
        if (fromDate == null || toDate == null || zone == null) {
            log.debug("Invalid args to getAttendanceSeriesForUserByDates: fromDate={} toDate={} zone={} action={} siteId={} projectId={}"
            , fromDate, toDate, zone, action, siteId, projectId);
            return List.of();
        }

        List<Long> clientIds = userService.getClientIdsForUser(userId);
        if (clientIds == null || clientIds.isEmpty()) {
            // devolver zeros para el rango pedido
            List<AttendancePunchPointDto> empty = new ArrayList<>();
            LocalDate d = fromDate;
            while (!d.isAfter(toDate)) {
                empty.add(new AttendancePunchPointDto(d.toString(), 0L));
                d = d.plusDays(1);
            }
            return empty;
        }

        //Normalizar action
        String normalizedAction = normalizeAction(action);

        // Convertir a OffsetDateTime semi-abierto [fromDate, toDate)
        OffsetDateTime fromOffset = fromDate.atStartOfDay(zone).toOffsetDateTime();
        OffsetDateTime toOffset = toDate.plusDays(1)
                                        .atStartOfDay(zone).toOffsetDateTime();

        log.debug("Service: fetching attendances userId={} clientIds={} from={} to={} zone={} action={} siteId={} projectId={}"
                                                    , userId, clientIds, fromOffset, toOffset, zone, normalizedAction, siteId, projectId);

        List<AttendancePunchProjection> projections =
                                  repo.findByClientIdsAndDateBetween(
                                      clientIds, fromOffset, toOffset, siteId, projectId, normalizedAction);
        //Mapear projection a Dto
        List<AttendancePunchDto> attPunchsDto =

           (projections == null)
                ? List.of()
                : projections.stream()
                    .map(AttendancePunchDto::fromProjection)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

        //Agrupar por dia
        Map<LocalDate, Long> grouped = attPunchsDto.stream()
                .filter(dto -> dto.ts() != null)
                .collect(Collectors.groupingBy(
                        dto -> dto.ts().toInstant().atZone(zone).toLocalDate(),
                        Collectors.counting()
                ));

        //Construir serie
        List<AttendancePunchPointDto> series = new ArrayList<>();
        LocalDate d = fromDate;
        while (!d.isAfter(toDate)) {
            Long y = grouped.getOrDefault(d, 0L);
            series.add(new AttendancePunchPointDto(d.toString(), y));
            d = d.plusDays(1);
        }

        return series;

      }



    @Override
    @Transactional(readOnly = true)
    public List<HourlyCountDto> getAttendanceSeriesForUserByHours(
            Long userId,
            LocalDate date,
            ZoneId zone,
            String action,
            Long siteId,
            Long projectId
    ) {
        if (date == null || zone == null) {
            log.debug("Invalid args to getAttendanceSeriesForUserByHours: date={} zone={} action={} siteId={} projectId={}",
                    date, zone, action, siteId, projectId);
            return List.of();
        }

        List<Long> clientIds = userService.getClientIdsForUser(userId);
        if (clientIds == null || clientIds.isEmpty()) {
            // devolver 24 filas con ceros
            List<HourlyCountDto> empty = new ArrayList<>(24);
            for (int h = 0; h < 24; h++) {
                empty.add(new HourlyCountDto(String.format("%02d", h), 0L));
            }
            return empty;
        }

        // Normalizar action (IN/OUT) — si quieres contar todas las marcaciones, permite action == null
        String normalizedAction = normalizeAction(action);

        // Window [date 00:00, next day 00:00) en la zona del cliente
        OffsetDateTime fromOffset = date.atStartOfDay(zone).toOffsetDateTime();
        OffsetDateTime toOffset = date.plusDays(1).atStartOfDay(zone).toOffsetDateTime();

        log.debug("Service: fetching hourly attendances (punches) userId={} clientIds={} date={} from={} to={} zone={} action={} siteId={} projectId={}",
                userId, clientIds, date, fromOffset, toOffset, zone, normalizedAction, siteId, projectId);

        // Obtener proyecciones de marcaciones (tu repo actual)
        List<AttendancePunchProjection> projections =
                repo.findByClientIdsAndDateBetween(clientIds, fromOffset, toOffset, siteId, projectId, normalizedAction);

        List<AttendancePunchDto> attPunchsDto =
                (projections == null)
                        ? List.of()
                        : projections.stream()
                            .map(AttendancePunchDto::fromProjection)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());

        // Agrupar por hora local (según zone)
        Map<Integer, Long> groupedByHour = attPunchsDto.stream()
                .filter(dto -> dto.ts() != null)
                .collect(Collectors.groupingBy(
                        dto -> dto.ts().toInstant().atZone(zone).getHour(),
                        Collectors.counting()
                ));

        // Construir serie con 24 puntos "00".."23"
        List<HourlyCountDto> series = new ArrayList<>(24);
        for (int h = 0; h < 24; h++) {
            Long count = groupedByHour.getOrDefault(h, 0L);
            series.add(new HourlyCountDto(String.format("%02d", h), count));
        }

        // Opcional: log debug con suma total para verificar
        long total = series.stream().mapToLong(s -> s.count()).sum();
        log.debug("Service: hourly punches totalCountForDate={} (date={})", total, date);

        return series;
    }

}