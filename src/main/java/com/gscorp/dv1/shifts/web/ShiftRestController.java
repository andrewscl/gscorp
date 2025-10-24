package com.gscorp.dv1.shifts.web;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gscorp.dv1.shifts.application.ShiftService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/shift-patterns")
@RequiredArgsConstructor
public class ShiftRestController {

    private final ShiftService shiftService;

    @PoastMapping("/create")
    public ResponseEntity<ShiftDto> createShift(
        @jakarta.validation.Valid @RequestBody CreateShiftRequest req,
        org.springframework.web.util.UriComponentsBuilder ucb) {
        var entity = Shift.builder()
            .name(req.name().trim())
            .description(req.description())
            .startTime(req.startTime())
            .endTime(req.endTime())
            .breakDurationMinutes(req.breakDurationMinutes())
            .build();
        var saved = shiftService.saveShift(entity);
        var location = ucb.path("/api/shifts/{id}").buildAndExpand(saved.getId()).toUri();

        var dto = new ShiftDto(
                            saved.getId(),
                            saved.getName(),
                            saved.getDescription(),
                            saved.getStartTime(),
                            saved.getEndTime(),
                            saved.getBreakDurationMinutes());

        return ResponseEntity.created(location).body(dto);
    }
    
}