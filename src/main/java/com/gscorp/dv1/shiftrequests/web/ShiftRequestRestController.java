package com.gscorp.dv1.shiftrequests.web;

import java.net.URI;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.gscorp.dv1.shiftrequests.application.ShiftRequestService;
import com.gscorp.dv1.shiftrequests.infrastructure.ShiftRequest;
import com.gscorp.dv1.shiftrequests.web.dto.CreateShiftRequestRequest;
import com.gscorp.dv1.shiftrequests.web.dto.ShiftRequestDto;
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

        //Crear shiftrequest
        ShiftRequest shiftRequest = shiftRequestService.create(req);
        ShiftRequestDto dto = ShiftRequestDto.fromEntity(shiftRequest);

        URI uri = ucb.path("/api/shift-requests/{id}").buildAndExpand(dto.id()).toUri();

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


        var saved = shiftRequestService.update(shift);

        return ResponseEntity.ok(ShiftRequestDto.fromEntity(saved));
    }
    
}