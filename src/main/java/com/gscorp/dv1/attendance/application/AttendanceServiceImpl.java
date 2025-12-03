package com.gscorp.dv1.attendance.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gscorp.dv1.attendance.infrastructure.AttendancePunch;
import com.gscorp.dv1.attendance.infrastructure.AttendancePunchProjection;
import com.gscorp.dv1.attendance.infrastructure.AttendancePunchRepo;
import com.gscorp.dv1.attendance.web.dto.AttendancePunchDto;
import com.gscorp.dv1.attendance.web.dto.CreateAttendancePunchRequest;
import com.gscorp.dv1.attendance.web.dto.HourlyCountDto;
import com.gscorp.dv1.components.ZoneResolver;
import com.gscorp.dv1.components.dto.ZoneResolutionResult;
import com.gscorp.dv1.sites.infrastructure.Site;
import com.gscorp.dv1.sites.infrastructure.SiteRepository;
import com.gscorp.dv1.users.application.UserService;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl implements AttendanceService {

  private final AttendancePunchRepo repo;
  private final SiteRepository siteRepo;
  private final UserService userService;
  private final ZoneResolver zoneResolver;

  private static final double MAX_DIST_METERS = 250.0;
  private static final long   MIN_GAP_SECONDS = 60;

  /** Encuentra el site más cercano a la ubicación dada */
  public Site findNearestSite(double lat, double lon) {
    List<Site> allSites = siteRepo.findAll();
    return allSites.stream()
        .min(Comparator.comparing(site -> haversineMeters(lat, lon, site.getLat(), site.getLon())))
        .orElse(null);
  }

  /** COMANDO: registrar marcación IN/OUT alternada usando el site más cercano */
  @Transactional
  public AttendancePunch punch(Long userId, double lat, double lon, Double acc, String ip, String ua, Site site) {
    var now  = OffsetDateTime.now();
    var last = repo.findFirstByUserIdOrderByTsDesc(userId).orElse(null);

    if (last != null && Duration.between(last.getTs(), now).getSeconds() < MIN_GAP_SECONDS) {
      return last; // anti doble click
    }

    // Busca el site más cercano a la marcación
    Site nearestSite = findNearestSite(lat, lon);
    if (nearestSite == null) throw new IllegalStateException("No hay sitios registrados");

    double siteLat = nearestSite.getLat();
    double siteLon = nearestSite.getLon();

    double dist = haversineMeters(lat, lon, siteLat, siteLon);
    boolean ok  = dist <= MAX_DIST_METERS;

    String nextAction = (last == null || "OUT".equalsIgnoreCase(last.getAction())) ? "IN" : "OUT";

    var p = AttendancePunch.builder()
        .userId(userId)
        .ts(now)
        .lat(lat).lon(lon).accuracyM(acc)
        .action(nextAction)
        .locationOk(ok).distanceM(dist)
        .ip(ip).deviceInfo(ua)
        .site(nearestSite)
        .build();

    // Si quieres guardar el ID del site más cercano, agrega este campo en AttendancePunch y setéalo aquí:
    // p.setSiteId(nearestSite.getId());

    return repo.save(p);
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

  /** Lista de marcaciones del usuario en el rango (detalle) */
  @Transactional(readOnly = true)
  public List<AttendancePunch> listForUser(Long userId, LocalDate from, LocalDate to, ZoneId zone) {
    var fromTs = from.atStartOfDay(zone).toOffsetDateTime();
    var toTs   = to.plusDays(1).atStartOfDay(zone).minusNanos(1).toOffsetDateTime();
    return repo.findByUserIdAndTsBetweenOrderByTsDesc(userId, fromTs, toTs);
  }

  /** Utilidad geodésica */
  public double haversineMeters(double lat1,double lon1,double lat2,double lon2){
    double R=6371000, dLat=Math.toRadians(lat2-lat1), dLon=Math.toRadians(lon2-lon1);
    double a=Math.sin(dLat/2)*Math.sin(dLat/2)
           + Math.cos(Math.toRadians(lat1))*Math.cos(Math.toRadians(lat2))
           * Math.sin(dLon/2)*Math.sin(dLon/2);
    return 2*R*Math.atan2(Math.sqrt(a),Math.sqrt(1-a));
  }

  public long countByClientIdAndDate(Long clientId, LocalDate date) {
    var fromTs = date.atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime();
    var toTs   = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).minusNanos(1).toOffsetDateTime();
    return repo.countByClientIdAndTsBetween(clientId, fromTs, toTs);
  }

  @Override
  @Transactional
  public AttendancePunch punch(CreateAttendancePunchRequest dto) {
    Site site = siteRepo.findById(dto.getSiteId())
        .orElseThrow(() -> new IllegalArgumentException("Site no encontrado: " + dto.getSiteId()));
    return punch(dto.getUserId(), dto.getLat(), dto.getLon(),
                  dto.getAccuracy(), dto.getIp(), dto.getDeviceInfo(), site);
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
              Long userId, LocalDate fromDate, LocalDate toDate, String clientTz) {

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
                                                .findDtoByClientIdsAndDateBetween(
                                                    clientIds, start, endExclusive);

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
    


}