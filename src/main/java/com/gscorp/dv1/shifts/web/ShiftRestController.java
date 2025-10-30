package com.gscorp.dv1.shifts.web;

import java.time.Duration;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.gscorp.dv1.shifts.application.ShiftService;
import com.gscorp.dv1.shifts.infrastructure.Shift;
import com.gscorp.dv1.shifts.web.dto.CreateShiftRequest;
import com.gscorp.dv1.shifts.web.dto.ShiftDto;
import com.gscorp.dv1.shifts.web.dto.ShiftRequestIdDto;
import com.gscorp.dv1.sites.application.SiteService;
import com.gscorp.dv1.sites.infrastructure.Site;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/shift-patterns")
@RequiredArgsConstructor
public class ShiftRestController {

    private final ShiftService shiftService;
    private final SiteService siteService;

    @PostMapping("/create")
    public ResponseEntity<ShiftDto> createShift(
        @jakarta.validation.Valid @RequestBody CreateShiftRequest req,
        org.springframework.web.util.UriComponentsBuilder ucb
    ) {
        // Buscar el sitio
        Site site = siteService.findById(req.siteId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sitio no encontrado"));

        // Crear Shift según tipo
        Shift entity;
        if ("SPORADIC".equalsIgnoreCase(req.type())) {
            entity = Shift.builder()
                .site(site)
                .startTs(req.shiftDateTime())
                .endTs(req.shiftDateTime().plusHours(
                    Duration.between(req.startTime(), req.endTime()).toHours()
                )) // Ajusta según tu lógica de duración
                .description(req.description())
                .build();
        } else { // FIXED
            // Para FIXED podrías crear un solo Shift, o varios si tienes lógica de generación masiva
            entity = Shift.builder()
                .site(site)
                .startTs(req.startDate().atTime(req.startTime()).atOffset(java.time.ZoneOffset.UTC))
                .endTs(req.startDate().atTime(req.endTime()).atOffset(java.time.ZoneOffset.UTC))
                .description(req.description())
                .build();
            // Si necesitas crear varios Shifts por rango, eso sería en un servicio aparte
        }

        var saved = shiftService.createShift(entity);
        var location = ucb.path("/api/shifts/{id}").buildAndExpand(saved.getId()).toUri();
        var shiftRequestIdDto = saved.getShiftRequest() == null ? null :
            new ShiftRequestIdDto(
                saved.getShiftRequest().getId()
            );

        var dto = new ShiftDto(
            saved.getId(),
            saved.getSite().getId(),
            saved.getSite().getName(),
            saved.getStartTs(),
            saved.getEndTs(),
            saved.getCode(),
            saved.getDescription(),
            saved.getShiftType(),
            saved.getWeekDays(),
            saved.getLunchTime(),
            saved.getShiftStatus(),
            saved.getPlannedGuards(),
            shiftRequestIdDto
        );

        return ResponseEntity.created(location).body(dto);
    }
    
}