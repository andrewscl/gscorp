package com.gscorp.dv1.operations.sitezones.web;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gscorp.dv1.config.security.SecurityUser;
import com.gscorp.dv1.enums.SiteZoneStatus;
import com.gscorp.dv1.operations.sitezones.application.SiteZoneService;
import com.gscorp.dv1.operations.sitezones.web.dto.SiteZoneDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/site-zones")
@RequiredArgsConstructor
public class SiteZoneRestController {

    private final SiteZoneService siteZoneService;

    @GetMapping("/list")
    public ResponseEntity<List<SiteZoneDto>> getSiteZones(
            @AuthenticationPrincipal SecurityUser securityUser,
            @RequestParam (required = false) UUID siteExternalId,
            @RequestParam (required = false) SiteZoneStatus status
    ) {
        if (securityUser == null) {
            throw new AuthenticationCredentialsNotFoundException("Usuario no autenticado");
        }
        UUID userExternalId = securityUser.getUser().getExternalId();
        List<SiteZoneDto> siteZones = siteZoneService.getSiteZones(
                userExternalId,
                siteExternalId,
                status
        );
        
        return ResponseEntity.ok(siteZones);
    }

}
