package com.gscorp.dv1.requests.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
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

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/shift-requests")
@RequiredArgsConstructor
public class ShiftRequestRestController {

    private final ShiftRequestService shiftRequestService;
    private final SiteService siteService;

    @PostMapping("/create")
    public ResponseEntity<ShiftRequestDto> createShiftRequest(
        @Valid @RequestBody CreateShiftRequestRequest req,
        UriComponentsBuilder ucb){

        //Resolver site
        Long siteId = req.siteId();
        Site site = siteService.findById(siteId)
                .orElseThrow(() -> new IllegalArgumentException("Sitio no encontrado: " + siteId));
        
        // Build entity
        var entity = ShiftRequest.builder()
            .site(site)
            .code(req.code().trim())
            .type(ShiftRequest.RequestType.valueOf(req.type()))
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

        //Save
        var saved = shiftRequestService.saveShiftRequest(entity);

        //Answer
        var location = ucb.path("/api/shift-requests/{id}")
            .buildAndExpand(saved.getId())
            .toUri();

        var dto = new ShiftRequestDto(
            saved.getId(),
            saved.getCode(),
            saved.getSite().getId(),
            saved.getSite().getName(),
            saved.getType(),
            saved.getStartDate(),
            saved.getEndDate(),
            saved.getWeekDays(),
            saved.getShiftDateTime(),
            saved.getStartTime(),
            saved.getEndTime(),
            saved.getLunchTime(),
            saved.getStatus(),
            saved.getDescription()
        );

        return ResponseEntity.created(location).body(dto);
    }

}
