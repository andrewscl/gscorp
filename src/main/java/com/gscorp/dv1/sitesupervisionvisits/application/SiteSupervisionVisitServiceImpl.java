package com.gscorp.dv1.sitesupervisionvisits.application;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gscorp.dv1.employees.application.EmployeeService;
import com.gscorp.dv1.sites.application.SiteService;
import com.gscorp.dv1.sitesupervisionvisits.infrastructure.SiteSupervisionVisit;
import com.gscorp.dv1.sitesupervisionvisits.infrastructure.SiteSupervisionVisitRepository;
import com.gscorp.dv1.sitesupervisionvisits.web.dto.CreateSiteSupervisionVisitDto;
import com.gscorp.dv1.sitesupervisionvisits.web.dto.SiteSupervisionVisitDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SiteSupervisionVisitServiceImpl implements SiteSupervisionVisitService{

    private final SiteSupervisionVisitRepository siteSupervisionVisitRepo;
    private final EmployeeService employeeService;
    private final SiteService siteService;

    @Override
    @Transactional
    public SiteSupervisionVisitDto saveSiteSupervisionVisit(
                                CreateSiteSupervisionVisitDto dto) {

        //Buscar empleado
        String username = SecurityContextHolder.getContext()
                            .getAuthentication().getName();

        // Buscar el empleado por el usuario (ajusta este método según tu EmployeeService)
        var employee = employeeService.findByUsername(username)
            .orElseThrow(() -> 
                new IllegalArgumentException("Empleado no encontrado para usuario: " + username));

        //Buscar sitio
        var site = siteService.findById(dto.siteId())
                    .orElseThrow(() -> 
                        new IllegalArgumentException("Sitio no encontrado: " + dto.siteId()));

        //Construir entidad
        var entity = SiteSupervisionVisit.builder()
            .employee(employee)
            .site(site)
            .description(dto.description())
            .photoPath(dto.photoPath())
            .videoPath(dto.videoPath())
            .build();

        //Guardar entidad
        var savedEntity = siteSupervisionVisitRepo.save(entity);

        return SiteSupervisionVisitDto.fromEntity(savedEntity);

    }
}
