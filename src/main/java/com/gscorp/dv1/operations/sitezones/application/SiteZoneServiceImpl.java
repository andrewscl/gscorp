package com.gscorp.dv1.operations.sitezones.application;

import java.util.List;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.gscorp.dv1.enums.SiteZoneStatus;
import com.gscorp.dv1.operations.sites.application.SiteService;
import com.gscorp.dv1.operations.sites.infrastructure.Site;
import com.gscorp.dv1.operations.sitezones.infrastructure.SiteZone;
import com.gscorp.dv1.operations.sitezones.infrastructure.SiteZoneRepository;
import com.gscorp.dv1.operations.sitezones.infrastructure.projections.SiteZoneProjection;
import com.gscorp.dv1.operations.sitezones.web.dto.CreateSiteZoneRequest;
import com.gscorp.dv1.operations.sitezones.web.dto.SiteZoneDto;
import com.gscorp.dv1.users.application.UserScopeService;
import com.gscorp.dv1.users.application.dto.ProjectScope;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SiteZoneServiceImpl implements SiteZoneService {

    private final UserScopeService userScopeService;
    private final SiteZoneRepository siteZoneRepository;
    private final SiteService siteService;

    @Transactional(readOnly = true)
    public List<SiteZoneDto> getSiteZones(
                                UUID userExternalId,
                                UUID siteId,
                                SiteZoneStatus status) {
        if (userExternalId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado");
        }
        ProjectScope scope = userScopeService.getProjectScope();
        List<SiteZoneProjection> projections = 
            siteZoneRepository.findByProjectIds(
                                scope.ignoreFilter(),
                                scope.projectIds(),
                                siteId,
                                status
                                );
        return projections.stream()
                .map(projection -> new SiteZoneDto(
                    projection.getId(),
                    projection.getExternalId(),
                    projection.getName(),
                    projection.getStatus()
                ))
                .toList();
    }


    @Transactional
    public SiteZoneDto createSiteZone (
                    UUID userExternalId,
                    CreateSiteZoneRequest request){
        if (userExternalId == null) {
            throw new IllegalArgumentException("Usuario no autenticado");
        }
        ProjectScope scope = userScopeService.getProjectScope();
        Site site = siteService.findByExternalId(
                        scope.ignoreFilter(), scope.projectIds(), request.siteExternalId())
                            .orElseThrow(() -> new EntityNotFoundException("El sitio no existe o no tienes permiso de acceso"));
        boolean exists = siteZoneRepository.existsBySiteIdAndNameIgnoreCase(site.getId(), request.name());
        if (exists) {
            throw new IllegalArgumentException(
                    "Ya existe una zona con el nombre '" + request.name().trim() + "' en este sitio."
            );
        }
        SiteZone siteZone = SiteZone.builder()
                                    .name(request.name())
                                    .site(site)
                                    .status(SiteZoneStatus.ACTIVE)
                                    .build();
        SiteZone savedSiteZone = siteZoneRepository.save(siteZone);
        return SiteZoneDto.fromEntity(savedSiteZone);
    }

    @Transactional
    public void delete (UUID userExternalId, UUID siteZoneExternalId) {
        if (userExternalId == null) {
            throw new IllegalArgumentException("Usuario no autenticado");
        }
        if(siteZoneExternalId == null) {
            throw new IllegalArgumentException("El siteZoneExternalId es requerido");
        }
        ProjectScope scope = userScopeService.getProjectScope();
        SiteZone siteZone = siteZoneRepository.findByExternalId(
                                            scope.ignoreFilter(),
                                            scope.projectIds(),
                                            siteZoneExternalId
            ).orElseThrow(() -> new EntityNotFoundException(
                "La zonano existe o no tienes acceso."));
        try {
            siteZoneRepository.delete(siteZone);
        } catch (DataIntegrityViolationException e){
            throw new IllegalArgumentException("No se puede eliminar la zona.");
        }
    }


}
