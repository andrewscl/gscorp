package com.gscorp.dv1.operations.shiftpatterns.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.gscorp.dv1.operations.shiftpatterns.application.ShiftPatternService;
import com.gscorp.dv1.operations.shiftpatterns.infrastructure.ShiftPattern;
import com.gscorp.dv1.operations.shiftpatterns.web.dto.CreateShiftPatternRequest;
import com.gscorp.dv1.operations.shiftpatterns.web.dto.ShiftPatternDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/shift-patterns")
@RequiredArgsConstructor
public class ShiftPatternRestController {

    private final ShiftPatternService shiftPatternService;

    @PostMapping("/create")
    public ResponseEntity<ShiftPatternDto> createShiftPattern(
        @Valid @RequestBody CreateShiftPatternRequest req,
        UriComponentsBuilder ucb) {
        var entity = ShiftPattern.builder()
            .name(req.name().trim())
            .description(req.description())
            .workDays(req.workDays())
            .restDays(req.restDays())
            .code(req.code())
            .build();
        var saved = shiftPatternService.saveShiftPattern(entity);
        var location = ucb.path("/api/shift-patterns/{id}").buildAndExpand(saved.getId()).toUri();

        var dto = new ShiftPatternDto(
                            saved.getId(),
                            saved.getExternalId(),
                            saved.getName(),
                            saved.getDescription(),
                            saved.getWorkDays(),
                            saved.getRestDays(),
                            saved.getCode()
        );

        return ResponseEntity.created(location).body(dto);
    }
}
