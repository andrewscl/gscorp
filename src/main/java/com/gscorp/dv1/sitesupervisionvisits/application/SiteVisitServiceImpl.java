package com.gscorp.dv1.sitesupervisionvisits.application;

import java.io.File;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.gscorp.dv1.components.ZoneResolver;
import com.gscorp.dv1.components.dto.ZoneResolutionResult;
import com.gscorp.dv1.employees.application.EmployeeService;
import com.gscorp.dv1.sites.application.SiteService;
import com.gscorp.dv1.sitesupervisionvisits.infrastructure.SiteVisit;
import com.gscorp.dv1.sitesupervisionvisits.infrastructure.SiteVisitDtoProjection;
import com.gscorp.dv1.sitesupervisionvisits.infrastructure.SiteVisitRepository;
import com.gscorp.dv1.sitesupervisionvisits.infrastructure.SiteVisitHourlyCountProjection;
import com.gscorp.dv1.sitesupervisionvisits.web.dto.CreateSiteSupervisionVisitRequest;
import com.gscorp.dv1.sitesupervisionvisits.web.dto.SiteVisitDto;
import com.gscorp.dv1.sitesupervisionvisits.web.dto.SiteVisitPointDto;
import com.gscorp.dv1.sitesupervisionvisits.web.dto.SiteVisitCountDto;
import com.gscorp.dv1.sitesupervisionvisits.web.dto.SiteVisitHourlyDto;
import com.gscorp.dv1.users.application.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SiteVisitServiceImpl implements SiteVisitService{

    private final SiteVisitRepository siteSupervisionVisitRepo;
    private final EmployeeService employeeService;
    private final SiteService siteService;
    private final UserService userService;
    private final ZoneResolver zoneResolver;

    @Value("${file.supervision_files-dir}")
    private String uploadFilesDir;

    @Override
    @Transactional
    public SiteVisitDto createSiteSupervisionVisitRequest(
                                CreateSiteSupervisionVisitRequest req, Long userId) {

        String photoPath = null, videoPath = null;
        try {

            //Fotografias
            MultipartFile photo = req.getPhoto();
            if(photo != null && !photo.isEmpty()) {
                // Generar el nombre del archivo
                String originalFilename = photo.getOriginalFilename();
                String fileExtension = "";
                if (originalFilename != null && originalFilename.contains(".")) {
                    fileExtension = originalFilename.substring(originalFilename.lastIndexOf('.'));
                }
                String storedFilename = UUID.randomUUID().toString() + fileExtension;

                //Directorio fisico donde se guardara el archivo
                File dest = new File(uploadFilesDir, "photos");
                if(!dest.exists()) dest.mkdirs();
                File storedFile = new File(dest, storedFilename);
                photo.transferTo(storedFile);
                photoPath = "/files/supervision_files/photos/" + storedFilename;
            }

            //Videos
            MultipartFile video = req.getVideo();
            if(video != null && !video.isEmpty()) {
                // Generar el nombre del archivo
                String originalFilename = video.getOriginalFilename();
                String fileExtension = "";
                if (originalFilename != null && originalFilename.contains(".")) {
                    fileExtension = originalFilename.substring(originalFilename.lastIndexOf('.'));
                }
                String storedFilename = UUID.randomUUID().toString() + fileExtension;

                //Directorio fisico donde se guardara el archivo
                File dest = new File(uploadFilesDir, "videos");
                if(!dest.exists()) dest.mkdirs();
                File storedFile = new File(dest, storedFilename);
                video.transferTo(storedFile);
                videoPath = "/files/supervision_files/videos/" + storedFilename;
            }
        } catch (Exception e) {
            throw new RuntimeException("Error al guardar archivos", e);
        }

        //Buscar empleado
        String username = SecurityContextHolder.getContext()
                            .getAuthentication().getName();

        // Buscar el empleado por el usuario (ajusta este método según tu EmployeeService)
        var employee = employeeService.findByUsername(username)
            .orElseThrow(() -> 
                new IllegalArgumentException("Empleado no encontrado para usuario: " + username));

        //Buscar sitio
        var site = siteService.findById(req.getSiteId())
                    .orElseThrow(() -> 
                        new IllegalArgumentException("Sitio no encontrado: " + req.getSiteId()));

        // -------------------------
        // Resolver visitDateTime con prioridad y registrar metadata de zona
        // Prioridad:
        // 1) req.getVisitDateTime() (OffsetDateTime enviado por cliente) -> usar tal cual
        // 2) Si no viene, resolver zona con ZoneResolver(userId, req.getClientTimeZone()) y usar "now" en esa zona
        // -------------------------
        OffsetDateTime visitDateTime;
        String clientTimezoneToStore = null;
        String timezoneSourceToStore = null;

        if (req.getVisitDateTime() != null) {
            // Cliente ya envió un OffsetDateTime (inequívoco)
            visitDateTime = req.getVisitDateTime();
            // Si el cliente también envió clientTimeZone preferirlo, si no guardamos el offset como referencia
            if (req.getClientTimeZone() != null && !req.getClientTimeZone().isBlank()) {
                clientTimezoneToStore = req.getClientTimeZone();
                timezoneSourceToStore = "CLIENT_REQUEST_TZ";
            } else {
                clientTimezoneToStore = visitDateTime.getOffset().toString(); // e.g. "-03:00"
                timezoneSourceToStore = "CLIENT_PROVIDED_OFFSET";
            }
        } else {
            // No vino OffsetDateTime: resolver zona (requested -> user profile -> system)
            ZoneResolutionResult zr = zoneResolver.resolveZone(userId, req.getClientTimeZone());
            ZoneId resolvedZone = zr.zoneId(); // espera getZone() y getSource() en ZoneResolutionResult
            visitDateTime = ZonedDateTime.now(resolvedZone).toOffsetDateTime();
            clientTimezoneToStore = resolvedZone.getId();
            timezoneSourceToStore = zr.source();
        }

        // Normalizar a UTC antes de persistir (recomendado para coherencia)
        OffsetDateTime visitDateTimeUtc = visitDateTime.withOffsetSameInstant(ZoneOffset.UTC);

        //Construir entidad
        var entity = SiteVisit.builder()
            .site(site)
            .employee(employee)
            .visitDateTime(visitDateTimeUtc)
            .latitude(req.getLatitude())
            .longitude(req.getLongitude())
            .description(req.getDescription())
            .photoPath(photoPath)
            .videoPath(videoPath)
            .clientTimezone(clientTimezoneToStore)
            .timezoneSource(timezoneSourceToStore)
            .build();

        SiteVisit savedEntity = siteSupervisionVisitRepo.save(entity);

        return SiteVisitDto.fromEntity(savedEntity);
    }



    @Override
    public List<SiteVisitDto> findByUserAndDateBetween(
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
        List<SiteVisitDtoProjection> rows = siteSupervisionVisitRepo
                                                .findDtoByUserAndDateBetween(clientIds, start, endExclusive);

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
            OffsetDateTime visitOffset = p.getVisitDateTime();
            if (visitOffset != null) {
                Instant instant = visitOffset.toInstant();
                ZonedDateTime local = instant.atZone(displayZone);
                formatted = local.format(fmt);
            }

            return new SiteVisitDto(
                p.getId(),
                p.getEmployeeId(),
                p.getEmployeeName(),
                p.getSiteId(),
                p.getSiteName(),
                p.getVisitDateTime(),
                p.getLatitude(),
                p.getLongitude(),
                p.getDescription(),
                p.getPhotoPath(),
                p.getVideoPath(),
                formatted
            );
        }).collect(Collectors.toList());

    }



    @Override
    public SiteVisitDto findByIdWithEmployeeAndSite(Long id) {
        var visit = siteSupervisionVisitRepo.findByIdWithEmployeeAndSite(id)
            .orElseThrow(() -> 
                new IllegalArgumentException("Visita de supervisión no encontrada: " + id));
        return SiteVisitDto.fromEntity(visit);
    }

    @Override
    public long countByClientIdAndDate(Long clientId, LocalDate date) {
    ZoneId zone = ZoneId.systemDefault();
    OffsetDateTime start = date.atStartOfDay(zone).toOffsetDateTime();
    OffsetDateTime end = date.plusDays(1).atStartOfDay(zone).toOffsetDateTime();
    return siteSupervisionVisitRepo.countByClientIdAndDateBetween(clientId, start, end);
    }


    @Override
    public List<SiteVisitCountDto> getVisitsBySite(Long clientId, OffsetDateTime from, OffsetDateTime to) {
        return siteSupervisionVisitRepo.findVisitsCountBySite(clientId, from, to);
    }



    @Override
    @Transactional(readOnly = true)
    public List<SiteVisitHourlyDto> getVisitsSeriesForUserByDateByVisitHourlyAgregated
                            (Long userId, LocalDate date, String tz) {

        // 1) resolver zona usando ZoneResolver (prioridad: requestedTz -> user profile -> system default)
        ZoneResolutionResult zr = zoneResolver.resolveZone(userId, tz);
        ZoneId zone = zr.zoneId();
        log.debug("Resolved zone {} (source={}) for userId={} requestedTz={}", zone, zr.source(), userId, tz);

        // 2) obtener clientIds asociados al usuario
        List<Long> clientIds = userService.getClientIdsForUser(userId);
        if (clientIds == null || clientIds.isEmpty()) {
            // devolver 24 ceros (00..23)
            return IntStream.range(0, 24)
                    .mapToObj(i -> new SiteVisitHourlyDto(String.format("%02d", i), 0L))
                    .collect(Collectors.toList());
        }

        // 3) construir rango [startOfDay, startOfNextDay) en la zona resuelta
        OffsetDateTime from = zoneResolver.toStartOfDay(date, zone);
        OffsetDateTime to = zoneResolver.toStartOfDay(date.plusDays(1), zone);

        log.debug("Fetching hourly visits aggregated for userId={} clients={} date={} zone={} from={} to={}",
                userId, clientIds, date, zone, from, to);

        // 4) obtener proyección (por site/hora) y sumar por hora (00..23)
        List<SiteVisitHourlyCountProjection> rows =
                siteSupervisionVisitRepo.findByClientIdsAndDateAndHourlyBetween(from, to, zone.getId(), clientIds);

        long[] sums = new long[24];
        if (rows != null) {
            for (SiteVisitHourlyCountProjection r : rows) {
                String hh = r.getHour(); // esperamos '00'..'23'
                int idx;
                try {
                    idx = Integer.parseInt(hh);
                } catch (Exception ex) {
                    log.debug("Invalid hour value '{}' in projection, defaulting to 0", hh);
                    idx = 0;
                }
                Number n = r.getCount(); // proyección define Number getCount()
                long cnt = n == null ? 0L : n.longValue();
                if (idx >= 0 && idx < 24) sums[idx] += cnt;
            }
        }

        // 5) construir lista ordenada para frontend usando el DTO SiteVisitHourlyDto(hour, count)
        return IntStream.range(0, 24)
                .mapToObj(i -> new SiteVisitHourlyDto(String.format("%02d", i), sums[i]))
                .collect(Collectors.toList());

    }




    @Override
    @Transactional(readOnly = true)
    public long countByClientIdsAndDate(List<Long> clientIds, LocalDate date, String tz) {
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

        return siteSupervisionVisitRepo.countByClientIdsAndTsBetween(clientIds, from, to);
    }

    /**
     * Conveniencia: cuenta para un único clientId delegando en el método que acepta lista.
     */
    @Override
    @Transactional(readOnly = true)
    public long countByClientIdAndDate(Long clientId, LocalDate date, String tz) {
        if (clientId == null) return 0L;
        return countByClientIdsAndDate(List.of(clientId), date, tz);
    }


    @Override
    @Transactional(readOnly = true)
    public List<SiteVisitPointDto> getVisitsSeriesForUserByDates
                            (Long userId, LocalDate fromDate, LocalDate toDate, ZoneId zone) {

        List<Long> clientIds = userService.getClientIdsForUser(userId);
        if (clientIds == null || clientIds.isEmpty()) {
            // devolver zeros para el rango pedido
            List<SiteVisitPointDto> empty = new ArrayList<>();
            LocalDate d = fromDate;
            while (!d.isAfter(toDate)) {
                empty.add(new SiteVisitPointDto(d.toString(), 0L));
                d = d.plusDays(1);
            }
            return empty;
        }

        // 2) convertir a OffsetDateTime semi-abierto [fromDate, toDate)
        OffsetDateTime fromOffset = fromDate.atStartOfDay(zone).toOffsetDateTime();
        OffsetDateTime toOffset = toDate.plusDays(1).atStartOfDay(zone).toOffsetDateTime();

        // 3) traer DTOs (repo devuelve SiteSupervisionVisitDto)
        log.debug("Service: fetching visits userId={} clientIds={} from={} to={} zone={}", userId, clientIds, fromOffset, toOffset, zone);
        List<SiteVisit> entities = siteSupervisionVisitRepo.findByClientIdsAndDateBetween(clientIds, fromOffset, toOffset);
        log.debug("Service: visits fetched count={}", entities == null ? null : entities.size());

        // 4.1)Mapear entidades a DTOs (usa el fromEntity que ya tienes en SiteSupervisionVisitDto)
        List<SiteVisitDto> visitsDto = (entities == null)
                ? List.of()
                : entities.stream()
                        .map(SiteVisitDto::fromEntity)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());

        // 4.2)Agrupar por fecha local en la zone
        Map<LocalDate, Long> grouped = visitsDto.stream()
                .filter(dto -> dto.visitDateTime() != null)
                .collect(Collectors.groupingBy(
                        dto -> dto.visitDateTime().toInstant().atZone(zone).toLocalDate(),
                        Collectors.counting()
                ));

        // 5) construir serie final
        List<SiteVisitPointDto> series = new ArrayList<>();
        LocalDate d = fromDate;
        while (!d.isAfter(toDate)) {
            long y = grouped.getOrDefault(d, 0L);
            series.add(new SiteVisitPointDto(d.toString(), y));
            d = d.plusDays(1);
        }

        return series;
    }

}
