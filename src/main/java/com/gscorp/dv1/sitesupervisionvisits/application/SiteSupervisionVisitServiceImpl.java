package com.gscorp.dv1.sitesupervisionvisits.application;

import java.io.File;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.gscorp.dv1.employees.application.EmployeeService;
import com.gscorp.dv1.sites.application.SiteService;
import com.gscorp.dv1.sitesupervisionvisits.infrastructure.SiteSupervisionVisit;
import com.gscorp.dv1.sitesupervisionvisits.infrastructure.SiteSupervisionVisitRepository;
import com.gscorp.dv1.sitesupervisionvisits.web.dto.CreateSiteSupervisionVisitRequest;
import com.gscorp.dv1.sitesupervisionvisits.web.dto.SiteSupervisionVisitDto;
import com.gscorp.dv1.sitesupervisionvisits.web.dto.SiteVisitCountDto;
import com.gscorp.dv1.sitesupervisionvisits.web.dto.SiteVisitHourlyDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SiteSupervisionVisitServiceImpl implements SiteSupervisionVisitService{

    private final SiteSupervisionVisitRepository siteSupervisionVisitRepo;
    private final EmployeeService employeeService;
    private final SiteService siteService;

    @Value("${file.supervision_files-dir}")
    private String uploadFilesDir;

    @Override
    @Transactional
    public SiteSupervisionVisitDto createSiteSupervisionVisitRequest(
                                CreateSiteSupervisionVisitRequest req) {

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

        //Construir entidad
        var entity = SiteSupervisionVisit.builder()
            .site(site)
            .employee(employee)
            .visitDateTime(OffsetDateTime.now())
            .latitude(req.getLatitude())
            .longitude(req.getLongitude())
            .description(req.getDescription())
            .photoPath(photoPath)
            .videoPath(videoPath)
            .build();

        SiteSupervisionVisit savedEntity = siteSupervisionVisitRepo.save(entity);

        return SiteSupervisionVisitDto.fromEntity(savedEntity);

    }

    @Override
    public List<SiteSupervisionVisitDto> getAllSiteSupervisionVisits() {
        return siteSupervisionVisitRepo.findAllWithEmployeeAndSite()
            .stream()
            .map(SiteSupervisionVisitDto::fromEntity)
            .collect(Collectors.toList());
    }

    @Override
    public SiteSupervisionVisitDto findByIdWithEmployeeAndSite(Long id) {
        var visit = siteSupervisionVisitRepo.findByIdWithEmployeeAndSite(id)
            .orElseThrow(() -> 
                new IllegalArgumentException("Visita de supervisión no encontrada: " + id));
        return SiteSupervisionVisitDto.fromEntity(visit);
    }

    @Override
    public long countByClientIdAndDate(Long clientId, LocalDate date) {
    ZoneId zone = ZoneId.systemDefault();
    OffsetDateTime start = date.atStartOfDay(zone).toOffsetDateTime();
    OffsetDateTime end = date.plusDays(1).atStartOfDay(zone).toOffsetDateTime();
    return siteSupervisionVisitRepo.countByClientIdAndDateBetween(clientId, start, end);
    }

    @Override
    public List<SiteSupervisionVisitDto> findByClientIdAndDateBetween
                        (Long clientId, OffsetDateTime from, OffsetDateTime to) {
        return siteSupervisionVisitRepo.findDtoByClientIdAndDateBetween(clientId, from, to);
    }

    @Override
    public List<SiteVisitCountDto> getVisitsBySite(Long clientId, OffsetDateTime from, OffsetDateTime to) {
        return siteSupervisionVisitRepo.findVisitsCountBySite(clientId, from, to);
    }


    @Override
    @Transactional(readOnly = true)
    public List <SiteVisitHourlyDto> getSiteVisitHourlyCounts(Collection<Long> clientIds, LocalDate date, String tz) {

        if(clientIds == null || clientIds.isEmpty()) {
            return Collections.emptyList();
        }

        ZoneId zone;

        try {
        zone = (tz == null || tz.isBlank()) ? ZoneId.of("UTC") : ZoneId.of(tz);
        } catch (Exception ex) {
        log.warn("Invalid tz '{}', falling back to UTC", tz);
        zone = ZoneId.of("UTC");
        }

        ZonedDateTime startZ = date.atStartOfDay(zone);
        ZonedDateTime endZ = startZ.plusDays(1);
        OffsetDateTime from = startZ.toOffsetDateTime();
        OffsetDateTime to = endZ.toOffsetDateTime();

        log.debug("getSiteVisitHourlyCounts clients={} date={} zone={} from={} to={}",
        clientIds, date, zone, from, to);

        List<SiteSupervisionVisitRepository.HourlySiteCount> rows = 
                            siteSupervisionVisitRepo.
                                findHourlySiteCountsForRange(from, to, zone.getId(), new ArrayList<>(clientIds));

        Map<Long, String> siteNames = new HashMap<>();
        Map<Long, Map<String, Long>> countsBySite = new HashMap<>();

        for (SiteSupervisionVisitRepository.HourlySiteCount r : rows) {
            Long sid = r.getSiteId();
            String sname = r.getSiteName();
            String hour = r.getHour() == null ? "00" : r.getHour();
            Long cnt = r.getCnt() == null ? 0L : r.getCnt();

            siteNames.putIfAbsent(sid, sname);
            countsBySite.computeIfAbsent(sid, k -> new HashMap<>())
                            .merge(hour, cnt, Long::sum);
        }

        // Build 24-row-per-site result
        List<Long> siteIds = countsBySite.keySet().stream().sorted().collect(Collectors.toList());
        List<SiteVisitHourlyDto> result = new ArrayList<>();

        for (Long sid : siteIds) {
        String sname = siteNames.getOrDefault(sid, "");
        Map<String, Long> hourly = countsBySite.get(sid);
        IntStream.range(0, 24).forEach(i -> {
            String hh = String.format("%02d", i);
            long c = hourly.getOrDefault(hh, 0L);
            result.add(new SiteVisitHourlyDto(sid, sname, hh, c));
        });
        }
        return result;
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

}
