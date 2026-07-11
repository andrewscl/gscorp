package com.gscorp.dv1.operations.shifts.web;

import java.time.ZoneId;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.gscorp.dv1.components.ZoneResolver;
import com.gscorp.dv1.components.dto.ZoneResolutionResult;
import com.gscorp.dv1.config.security.SecurityUser;
import com.gscorp.dv1.operations.shiftrequests.infrastructure.ShiftRequest;
import com.gscorp.dv1.operations.shiftrequests.infrastructure.ShiftRequestRepository;
import com.gscorp.dv1.operations.shifts.application.ShiftService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/shifts")
@RequiredArgsConstructor
public class ShiftRestController {

    private final ShiftService shiftService;
    private final ShiftRequestRepository shiftRequestRepo;
    private final ZoneResolver zoneResolver;


    @PostMapping("/create/{shiftRequestExternalId}/{clientTz}")
    public ResponseEntity<?> createShifts(
        @AuthenticationPrincipal SecurityUser securityUser,
        @PathVariable("shiftRequestExternalId") UUID shiftRequestExternalId,
        @PathVariable("clientTz") String clientTz
    ){
        if (securityUser == null) {
            log.warn("Intento de acceso no autenticado");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado.");
        }
        String username = securityUser.getUser().getUsername();
        UUID userExternalId = securityUser.getUser().getExternalId();
        ShiftRequest shiftRequest = shiftRequestRepo.findByExternalId(shiftRequestExternalId)
                .orElseThrow(() ->
                    new EntityNotFoundException(
                            "No shift request found with external ID: " + shiftRequestExternalId)
                );

        String cleanClientTz =
            (clientTz == null || clientTz.isBlank()) ? null : clientTz.trim();
        ZoneResolutionResult zoneResult =
                        zoneResolver.resolveZone(userExternalId, cleanClientTz);
        ZoneId zoneId = zoneResult.zoneId();

        try {
            shiftService.generateShiftsForNext30days(shiftRequest, username, zoneId);

            return ResponseEntity.ok("Turnos generados correctamente");

        } catch (Exception ex) {
            log.error("error when create shift records for shift request: " +
                                                        shiftRequestExternalId, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body("Error al generar los turnos " + ex.getMessage());
        }
    }


    @PostMapping("/create-bulk-approved")
    public ResponseEntity<?> createBulkApprovedShifts(
                @AuthenticationPrincipal SecurityUser securityUser
    ){
        if (securityUser == null) {
            log.warn("Intento de acceso no autenticado");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado.");
        }

        try {
            shiftService.processApprovedShiftRequests();
            return ResponseEntity.ok("Proceso masivo de generación de turnos iniciado/finalizado correctamente");
        } catch (Exception ex) {
            log.error("Error al generar masivamente los registros de turnos", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al generar los turnos masivos: " + ex.getMessage());
        }
    }




}