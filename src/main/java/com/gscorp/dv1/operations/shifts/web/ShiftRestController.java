package com.gscorp.dv1.operations.shifts.web;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.gscorp.dv1.components.ZoneResolver;
import com.gscorp.dv1.components.dto.ZoneResolutionResult;
import com.gscorp.dv1.config.security.SecurityUser;
import com.gscorp.dv1.enums.ShiftRequestStatus;
import com.gscorp.dv1.operations.shiftrequests.infrastructure.ShiftRequest;
import com.gscorp.dv1.operations.shiftrequests.infrastructure.ShiftRequestRepository;
import com.gscorp.dv1.operations.shifts.application.ShiftService;
import com.gscorp.dv1.operations.shifts.web.dto.CreateShift;
import com.gscorp.dv1.operations.shifts.web.dto.ShiftDto;
import com.gscorp.dv1.operations.shifts.web.dto.ShiftsCountLast24HoursDto;

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


    @PostMapping("/create/{shiftRequestExternalId}")
    public ResponseEntity<?> createShifts(
        @AuthenticationPrincipal SecurityUser securityUser,
        @PathVariable("shiftRequestExternalId") UUID shiftRequestExternalId,
        @RequestBody CreateShift createShift
    ){
        if (securityUser == null) {
            log.warn("Intento de acceso no autenticado");
            throw new
                ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado.");
        }
        String username = securityUser.getUser().getUsername();
        UUID userExternalId = securityUser.getUser().getExternalId();

        ShiftRequest shiftRequest = shiftRequestRepo.findByExternalId(shiftRequestExternalId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "No shift request found with external ID: " + shiftRequestExternalId));

        if(shiftRequest.getStatus() != ShiftRequestStatus.APPROVED) {
            return ResponseEntity
                    .status(400)
                    .body(Collections.singletonMap("error", "La solicitud debe estar aprobada para poder generar turnos."));
        }

        String cleanClientTz = (createShift.clientTz() == null ||
                        createShift.clientTz().isBlank()) ? null : createShift.clientTz().trim();
        ZoneResolutionResult zoneResult = zoneResolver.resolveZone(userExternalId, cleanClientTz);
        ZoneId zoneId = zoneResult.zoneId();

        shiftService.generateShiftsForNext30days(shiftRequest, username, zoneId);

        return ResponseEntity.ok("Turnos generados correctamente");
    }


    @PostMapping("/create-bulk-approved")
    public ResponseEntity<?> createBulkApprovedShifts(
                @AuthenticationPrincipal SecurityUser securityUser
    ){
        if (securityUser == null) {
            log.warn("Intento de acceso no autenticado");
            throw new
                ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Usuario no autenticado.");
        }

        try {
            shiftService.processApprovedShiftRequests();
            return ResponseEntity
                .ok("Proceso masivo de generación de turnos iniciado/finalizado correctamente");
        } catch (Exception ex) {
            log.error("Error al generar masivamente los registros de turnos", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al generar los turnos masivos: " + ex.getMessage());
        }
    }

    @GetMapping("/last-shifts/{shiftRequestExternalId}/{shiftsToShow}")
    public ResponseEntity<Page<ShiftDto>> getLastShiftsByShiftRequest(
                @AuthenticationPrincipal SecurityUser securityUser,
                @PathVariable("shiftRequestExternalId") UUID shiftRequestExternalId,
                @PathVariable("shiftsToShow") int shiftsToShow,
                @RequestParam(required = false) String zoneId
    ){
        if (securityUser == null) {
            throw new AuthenticationCredentialsNotFoundException("Usuario no autenticado");
        }
        UUID userExternalId = securityUser.getUser().getExternalId();
        Page<ShiftDto> shifts = shiftService.getLastShiftsByShiftRequest(
                                userExternalId, shiftRequestExternalId, shiftsToShow, zoneId);

        return ResponseEntity.ok(shifts);
    }

    @GetMapping("/last-24hours-shifts")
    public ResponseEntity<List<ShiftsCountLast24HoursDto>> getShiftsCountLast24Hours (
                @AuthenticationPrincipal SecurityUser securityUser
    ){
        if (securityUser == null) {
            throw new AuthenticationCredentialsNotFoundException("Usuario no autenticado");
        }
        UUID externalId = securityUser.getUser().getExternalId();

        return ResponseEntity.ok(shiftService.getShiftsCountLast24Hours(externalId));
    }

    @GetMapping("/next-available-shift/shift-request/{shiftRequestExternalId}/shift")
    public ResponseEntity<ShiftDto> getNextUnplannedShift (
                @AuthenticationPrincipal SecurityUser securityUser,
                @PathVariable("shiftRequestExternalId") UUID shiftRequestExternalId,
                @RequestParam(required = false) String clientZoneId,
                @RequestParam(required = false)
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startAssignmentDate
    ){
        if (securityUser == null) {
            throw new AuthenticationCredentialsNotFoundException("Usuario no autenticado");
        }
        UUID userExternalId = securityUser.getUser().getExternalId();
        ZoneResolutionResult zoneResult = zoneResolver.resolveZone(userExternalId, clientZoneId);
        ZoneId zoneId = zoneResult.zoneId();
        return  shiftService.getNextUnplannedShift(zoneId, shiftRequestExternalId, startAssignmentDate)
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.noContent().build());
    }


    @GetMapping("/next-shifts/shift-request/{shiftRequestExternalId}/shift")
    public ResponseEntity<List<ShiftDto>> getUpComingShiftsByShiftRequest (
                @AuthenticationPrincipal SecurityUser securityUser,
                @PathVariable("shiftRequestExternalId") UUID shiftRequestExternalId,
                @RequestParam(required = false) String clientZoneId,
                @RequestParam(required = false)
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate queryDate,
                @RequestParam(defaultValue = "5") int limit
    ){
        if (securityUser == null) {
            throw new AuthenticationCredentialsNotFoundException("Usuario no autenticado");
        }
        UUID userExternalId = securityUser.getUser().getExternalId();
        ZoneResolutionResult zoneResult = zoneResolver.resolveZone(userExternalId, clientZoneId);
        ZoneId zoneId = zoneResult.zoneId();
        List<ShiftDto> shifts = shiftService.getUpComingShiftsByShiftRequest(zoneId, shiftRequestExternalId, queryDate, limit);
        return ResponseEntity.ok(shifts);
    }

}
