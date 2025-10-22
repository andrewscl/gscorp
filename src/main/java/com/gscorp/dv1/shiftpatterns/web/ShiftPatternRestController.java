package com.gscorp.dv1.shiftpatterns.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gscorp.dv1.shiftpatterns.application.ShiftPatternService;
import com.gscorp.dv1.shiftpatterns.infrastructure.ShiftPattern;
import com.gscorp.dv1.shiftpatterns.web.dto.CreateShiftPatternRequest;
import com.gscorp.dv1.shiftpatterns.web.dto.ShiftPatternDto;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/positions")
@RequiredArgsConstructor
public class ShiftPatternRestController {

    private final ShiftPatternService shiftPatternService;

    @PostMapping("/create")
    public ResponseEntity<ShiftPatternDto> createShiftPattern(
        @jakarta.validation.Valid @RequestBody CreateShiftPatternRequest req,
        org.springframework.web.util.UriComponentsBuilder ucb) {
        var entity = ShiftPattern.builder()
            .name(req.name().trim())
            .description(req.description())
            .workDays(req.workDays())
            .restDays(req.restDays())
            .build();
        var saved = shiftPatternService.saveShiftPattern(entity);
        var location = ucb.path("/api/shift-patterns/{id}").buildAndExpand(saved.getId()).toUri();

        var dto = new ShiftPatternDto(
                            saved.getId(),
                            saved.getName(),
                            saved.getDescription(),
                            saved.getWorkDays(),
                            saved.getRestDays());

        return ResponseEntity.created(location).body(dto);
    }
}
