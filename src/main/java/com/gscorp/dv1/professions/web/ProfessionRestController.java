package com.gscorp.dv1.professions.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gscorp.dv1.professions.application.ProfessionService;
import com.gscorp.dv1.professions.infrastructure.Profession;
import com.gscorp.dv1.professions.web.dto.CreateProfessionRequest;
import com.gscorp.dv1.professions.web.dto.ProfessionDto;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/professions")
@RequiredArgsConstructor
public class ProfessionRestController {
    
    private final ProfessionService professionService;

    @PostMapping("/create")
    public ResponseEntity<ProfessionDto> createProfession(
        @jakarta.validation.Valid @RequestBody CreateProfessionRequest req,
        org.springframework.web.util.UriComponentsBuilder ucb) {
        var entity = Profession.builder()
            .name(req.name().trim())
            .description(req.description())
            .active(Boolean.TRUE.equals(req.active()))
            .code(req.code())
            .category(req.category())
            .level(req.level())
            .build();
        var saved = professionService.saveProfession(entity);
        var location = ucb.path("/api/professions/{id}").buildAndExpand(saved.getId()).toUri();

        var dto = ProfessionDto.fromEntity(saved);

        return ResponseEntity.created(location).body(dto);
    }
}
