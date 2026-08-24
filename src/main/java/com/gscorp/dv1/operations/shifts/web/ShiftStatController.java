package com.gscorp.dv1.operations.shifts.web;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.gscorp.dv1.config.security.SecurityUser;
import com.gscorp.dv1.operations.shifts.application.ShiftStatService;
import com.gscorp.dv1.operations.shifts.web.dto.statistics.ProjectSiteShiftsSummaryDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/shifts")
@RequiredArgsConstructor
public class ShiftStatController {

    private final ShiftStatService shiftStatService;

    @GetMapping("/getLast24Hours")
    public ResponseEntity<List<ProjectSiteShiftsSummaryDto>> getLast24Hours(
        @AuthenticationPrincipal SecurityUser securityUser,
        @RequestParam(required = false) String zoneIdStr
    ){
        if (securityUser == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado.");
        }
        UUID userExternalId = securityUser.getUser().getExternalId();
        List<ProjectSiteShiftsSummaryDto> dtos = 
            shiftStatService.getLast24hoursProjectSiteShiftsSummary(
                                            userExternalId, zoneIdStr);
        return ResponseEntity.ok(dtos);
    }

}
