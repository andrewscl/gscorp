package com.gscorp.dv1.operations.sitezones.application;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.gscorp.dv1.enums.SiteZoneStatus;
import com.gscorp.dv1.operations.sitezones.infrastructure.SiteZoneRepository;
import com.gscorp.dv1.operations.sitezones.infrastructure.projections.SiteZoneProjection;
import com.gscorp.dv1.operations.sitezones.web.dto.SiteZoneDto;
import com.gscorp.dv1.users.application.UserScopeService;
import com.gscorp.dv1.users.application.dto.ProjectScope;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SiteZoneServiceImpl implements SiteZoneService {

    private final UserScopeService userScopeService;
    private final SiteZoneRepository siteZoneRepository;

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

}
