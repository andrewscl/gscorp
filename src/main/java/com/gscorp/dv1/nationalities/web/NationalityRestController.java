package com.gscorp.dv1.nationalities.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gscorp.dv1.nationalities.application.NationalityService;
import com.gscorp.dv1.nationalities.infrastructure.Nationality;
import com.gscorp.dv1.nationalities.web.dto.CreateNationalityRequest;
import com.gscorp.dv1.nationalities.web.dto.NationalityDto;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/nationalities")
@RequiredArgsConstructor
public class NationalityRestController {

    private final NationalityService nationalityService;

    @PostMapping("/create")
    public ResponseEntity<NationalityDto> createNationality(
        @jakarta.validation.Valid @RequestBody CreateNationalityRequest req,
        org.springframework.web.util.UriComponentsBuilder ucb) {
        var entity = Nationality.builder()
            .name(req.name().trim())
            .isoCode(req.isoCode().trim())
            .build();
        var saved = nationalityService.saveNationality(entity);
        var location = ucb.path("/api/nationalities/{id}").buildAndExpand(saved.getId()).toUri();

        var dto = new NationalityDto(
                            saved.getId(),
                            saved.getName(),
                            saved.getIsoCode()
                            );

        return ResponseEntity.created(location).body(dto);
    }

}
