package com.gscorp.dv1.positions.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gscorp.dv1.positions.application.PositionService;
import com.gscorp.dv1.positions.infrastructure.Position;
import com.gscorp.dv1.positions.web.dto.CreatePositionRequest;
import com.gscorp.dv1.positions.web.dto.PositionDto;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/positions")
@RequiredArgsConstructor
public class PositionRestController {

    private final PositionService positionService;

    @PostMapping("/create")
    public ResponseEntity<PositionDto> createPosition(
        @jakarta.validation.Valid @RequestBody CreatePositionRequest req,
        org.springframework.web.util.UriComponentsBuilder ucb) {
        var entity = Position.builder()
            .name(req.name().trim())
            .description(req.description())
            .active(Boolean.TRUE.equals(req.active()))
            .code(req.code())
            .level(req.level())
            .build();
        var saved = positionService.savePosition(entity);
        var location = ucb.path("/api/positions/{id}").buildAndExpand(saved.getId()).toUri();

        var dto = new PositionDto(
                            saved.getId(),
                            saved.getName(),
                            saved.getDescription(),
                            saved.getActive(),
                            saved.getCode(),
                            saved.getLevel());

        return ResponseEntity.created(location).body(dto);
    }
}
