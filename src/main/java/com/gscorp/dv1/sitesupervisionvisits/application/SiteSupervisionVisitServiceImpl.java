package com.gscorp.dv1.sitesupervisionvisits.application;

import java.io.File;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SiteSupervisionVisitServiceImpl implements SiteSupervisionVisitService{

    private final SiteSupervisionVisitRepository siteSupervisionVisitRepo;
    private final EmployeeService employeeService;
    private final SiteService siteService;

    @Value("${file.supervision_photos-dir}")
    private String uploadPhotosDir;

    @Value("${file.supervision_videos-dir}")
    private String uploadVideosDir;

    @Override
    @Transactional
    public SiteSupervisionVisitDto createSiteSupervisionVisitRequest(
                                CreateSiteSupervisionVisitRequest req) {

        String photoPath = null, videoPath = null;
        try {
            MultipartFile photo = req.getPhoto();
            if(photo != null && !photo.isEmpty()) {
                // Lógica para guardar la foto y obtener la ruta
                String filename = UUID.randomUUID() + "_" + photo.getOriginalFilename();
                File dest = new
                    File(uploadPhotosDir + File.separator + filename);

                dest.getParentFile().mkdirs();
                photo.transferTo(dest);
                photoPath = uploadPhotosDir + "/" + filename;
            }
            MultipartFile video = req.getVideo();
            if(video != null && !video.isEmpty()) {
                // Lógica para guardar el video y obtener la ruta
                String filename = UUID.randomUUID() + "_" + video.getOriginalFilename();
                File dest = new
                    File(uploadVideosDir + File.separator + filename);

                dest.getParentFile().mkdirs();
                video.transferTo(dest);
                videoPath = uploadVideosDir + "/" + filename;
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
    public List<SiteSupervisionVisitDto> findByClientIdAndDateBetween(Long clientId, LocalDate fromDate, LocalDate toDate) {
        ZoneId zone = ZoneId.systemDefault();
        OffsetDateTime fromDateTime = fromDate.atStartOfDay(zone).toOffsetDateTime();
        OffsetDateTime toDateTime = toDate.plusDays(1).atStartOfDay(zone).toOffsetDateTime();
        return siteSupervisionVisitRepo
            .findByClientIdAndDateBetween(clientId, fromDateTime, toDateTime)
            .stream()
            .map(SiteSupervisionVisitDto::fromEntity)
            .collect(Collectors.toList());
    }

    @Override
    public long countByClientIdAndDate(Long clientId, LocalDate date) {
    ZoneId zone = ZoneId.systemDefault();
    OffsetDateTime start = date.atStartOfDay(zone).toOffsetDateTime();
    OffsetDateTime end = date.plusDays(1).atStartOfDay(zone).toOffsetDateTime();
    return siteSupervisionVisitRepo.countByClientIdAndDateBetween(clientId, start, end);
}

}
