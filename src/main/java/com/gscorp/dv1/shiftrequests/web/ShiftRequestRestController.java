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
import com.gscorp.dv1.shiftrequests.web.dto.CreateShiftRequestRequest;
import com.gscorp.dv1.shiftrequests.web.dto.ShiftRequestDto;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/shift-requests")
@RequiredArgsConstructor
public class ShiftRequestRestController {

    private final ShiftRequestService shiftRequestService;

    @PostMapping("/create")
    public ResponseEntity<ShiftRequestDto> createShiftRequest(
        @jakarta.validation.Valid @RequestBody CreateShiftRequestRequest req,
        UriComponentsBuilder ucb) {

        //Crear shiftrequest
        ShiftRequestDto dto = shiftRequestService.create(req);

        URI uri = ucb.path("/api/shift-requests/{id}").buildAndExpand(dto.id()).toUri();

        return ResponseEntity.created(uri).body(dto);
    }

    @PutMapping("/edit/{id}")
    public ResponseEntity<ShiftRequestDto> editShiftRequest(
        @PathVariable Long id,
        @jakarta.validation.Valid @RequestBody CreateShiftRequestRequest req
    ) {
        Optional<ShiftRequestDto> updatedDtoOpt = shiftRequestService.update(id, req);

        return updatedDtoOpt
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }
    
}