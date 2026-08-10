package com.gscorp.dv1.operations.shiftassignments.web;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import com.gscorp.dv1.config.security.SecurityUser;
import com.gscorp.dv1.operations.shiftassignments.application.ShiftAssignmentService;
import com.gscorp.dv1.operations.shiftassignments.web.dto.CreateShiftAssignmentRequest;
import com.gscorp.dv1.operations.shiftassignments.web.dto.ShiftAssignmentDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/shift-assignments")
@RequiredArgsConstructor
public class ShiftAssignmentRestController {

    private final ShiftAssignmentService shiftAssignmentService;
    
    @PostMapping
    public ResponseEntity<ShiftAssignmentDto> createShiftAssignment (
            @Valid @RequestBody CreateShiftAssignmentRequest request,
            @RequestHeader(value = "X-Time-Zone", required = false) String requestedZone,
            @AuthenticationPrincipal SecurityUser securityUser,
            UriComponentsBuilder ucb
    ){
        if (securityUser == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Usuario no autenticado.");
        }
        UUID userExternalId = securityUser.getUser().getExternalId();
        ShiftAssignmentDto saved = 
                shiftAssignmentService.createShiftAssignment(
                    userExternalId, requestedZone, request);

        var location = ucb.path("/api/shift-assignments/{externalId}")
                            .buildAndExpand(saved.externalId())
                            .toUri();

        return ResponseEntity.created(location).body(saved);
    }

}
