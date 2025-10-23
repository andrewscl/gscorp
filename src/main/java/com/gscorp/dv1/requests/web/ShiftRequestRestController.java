package com.gscorp.dv1.requests.web;

import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.gscorp.dv1.requests.application.ShiftRequestService;
import com.gscorp.dv1.requests.infrastructure.ShiftRequest;
import com.gscorp.dv1.requests.web.dto.CreateShiftRequestRequest;
import com.gscorp.dv1.requests.web.dto.ShiftRequestDto;
import com.gscorp.dv1.sites.application.SiteService;
import com.gscorp.dv1.sites.infrastructure.Site;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/shift-requests")
@RequiredArgsConstructor
public class ShiftRequestRestController {

    private final ShiftRequestService shiftRequestService;
    private final SiteService siteService;

    @PostMapping("/create")
    public ResponseEntity<ShiftRequestDto> createShiftRequest(
        @jakarta.validation.Valid @RequestBody CreateShiftRequestRequest req,
        UriComponentsBuilder ucb) {

        // Busca el site por Id usando Optional
        Optional<Site> siteOpt = siteService.findById(req.siteId());
        if (siteOpt.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        Site site = siteOpt.get();

        // Construye la entidad normalmente
        var entity = ShiftRequest.builder()
            .code(req.code())
            .site(site)
            .type(req.type() != null ? ShiftRequest.RequestType.valueOf(req.type()) : null)
            .startDate(req.startDate())
            .endDate(req.endDate())
            .weekDays(req.weekDays())
            .shiftDateTime(req.shiftDateTime())
            .startTime(req.startTime())
            .endTime(req.endTime())
            .lunchTime(req.lunchTime())
            .status(req.status())
            .description(req.description())
            .build();

        shiftRequestService.create(entity); // O save(entity), según tu service
        var dto = ShiftRequestDto.fromEntity(entity);
        var uri = ucb.path("/api/shift-requests/{id}").buildAndExpand(dto.id()).toUri();
        return ResponseEntity.created(uri).body(dto);
    }

    @PutMapping("/edit/{id}")
    public ResponseEntity<ShiftRequestDto> editShiftRequest(
        @PathVariable Long id,
        @jakarta.validation.Valid @RequestBody CreateShiftRequestRequest req
    ) {
        Optional<ShiftRequest> shiftOpt = shiftRequestService.findById(id);
        if (shiftOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        ShiftRequest shift = shiftOpt.get();

        // Si necesitas cambiar el site:
        if (req.siteId() != null) {
            Optional<Site> siteOpt = siteService.findById(req.siteId());
            if (siteOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(null);
            }
            shift.setSite(siteOpt.get());
        }

        // Actualiza los campos editables
        shift.setCode(req.code());
        shift.setType(req.type() != null ? ShiftRequest.RequestType.valueOf(req.type().trim().toUpperCase()) : null);
        shift.setStartDate(req.startDate());
        shift.setEndDate(req.endDate());
        shift.setWeekDays(req.weekDays());
        shift.setShiftDateTime(req.shiftDateTime());
        shift.setStartTime(req.startTime());
        shift.setEndTime(req.endTime());
        shift.setLunchTime(req.lunchTime());
        shift.setStatus(req.status());
        shift.setDescription(req.description());

        var saved = shiftRequestService.update(shift);

        return ResponseEntity.ok(ShiftRequestDto.fromEntity(saved));
    }
    
}