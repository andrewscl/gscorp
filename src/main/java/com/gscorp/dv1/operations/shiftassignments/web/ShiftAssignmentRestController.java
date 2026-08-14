package com.gscorp.dv1.operations.shiftassignments.web;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import com.gscorp.dv1.config.security.SecurityUser;
import com.gscorp.dv1.operations.shiftassignments.application.ShiftAssignmentLifeCycleService;
import com.gscorp.dv1.operations.shiftassignments.application.ShiftAssignmentService;
import com.gscorp.dv1.operations.shiftassignments.web.dto.CloseShiftAssignmentRequest;
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
    private final ShiftAssignmentLifeCycleService shiftAssignmentLifeCycleService;
    
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


    @PatchMapping("/close/{shiftAssignmentExternalId}")
    public ResponseEntity<ShiftAssignmentDto> closeShiftAssignment (
            @PathVariable("shiftAssignmentExternalId") UUID shiftAssignmentExternalId,
            @Valid @RequestBody CloseShiftAssignmentRequest request,
            @RequestHeader(value = "X-Time-Zone", required = false) String requestedZone,
            @AuthenticationPrincipal SecurityUser securityUser
    ){
        if (securityUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        UUID userExternalId = securityUser.getUser().getExternalId();
        String userName = securityUser.getUser().getUsername();
        shiftAssignmentLifeCycleService.closeAssignment(
                        userExternalId, 
                        shiftAssignmentExternalId, 
                        request.status(), 
                        request.assignmentEndDate(), 
                        userName, 
                        request.reason(),
                        requestedZone);
        ShiftAssignmentDto shiftAssignment = shiftAssignmentService.getByExternalId(shiftAssignmentExternalId);
        return ResponseEntity.ok(shiftAssignment);
    }

}
