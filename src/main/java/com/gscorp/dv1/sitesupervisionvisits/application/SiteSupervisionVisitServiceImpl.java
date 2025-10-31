package com.gscorp.dv1.sitesupervisionvisits.application;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gscorp.dv1.employees.infrastructure.Employee;
import com.gscorp.dv1.sites.application.SiteService;
import com.gscorp.dv1.sitesupervisionvisits.infrastructure.SiteSupervisionVisit;
import com.gscorp.dv1.sitesupervisionvisits.infrastructure.SiteSupervisionVisitRepository;
import com.gscorp.dv1.sitesupervisionvisits.web.dto.CreateSiteSupervisionVisitDto;
import com.gscorp.dv1.sitesupervisionvisits.web.dto.SiteSupervisionVisitDto;
import com.gscorp.dv1.users.application.UserService;
import com.gscorp.dv1.users.infrastructure.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SiteSupervisionVisitServiceImpl implements SiteSupervisionVisitService{

    private final SiteSupervisionVisitRepository siteSupervisionVisitRepo;
    private final SiteService siteService;
    private final UserService userService;

    @Override
    @Transactional
    public SiteSupervisionVisitDto saveSiteSupervisionVisit(
                                CreateSiteSupervisionVisitDto dto) {

        //Buscar usuario y empleado
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userService.findByUsername(username)
                        .orElseThrow(() -> 
                            new IllegalArgumentException("Usuario no encontrado: " + username));

        Employee supervisor = user.getEmployee();
        if (supervisor == null) {
            throw new IllegalStateException("El usuario autenticado no tiene un empleado asociado.");
        }

        //Buscar sitio
        var site = siteService.findById(dto.siteId())
                    .orElseThrow(() -> 
                        new IllegalArgumentException("Sitio no encontrado: " + dto.siteId()));

        //Construir entidad
        var entity = SiteSupervisionVisit.builder()
            .supervisor(supervisor)
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
