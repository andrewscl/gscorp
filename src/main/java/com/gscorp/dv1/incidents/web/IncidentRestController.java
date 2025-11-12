package com.gscorp.dv1.incidents.web;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.gscorp.dv1.incidents.application.IncidentService;
import com.gscorp.dv1.incidents.web.dto.CreateIncidentRequest;
import com.gscorp.dv1.incidents.web.dto.IncidentDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/incidents")
@RequiredArgsConstructor
public class IncidentRestController {

    private final IncidentService incidentService;
    
    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<IncidentDto> createIncident(
        @Valid @ModelAttribute CreateIncidentRequest req,
        UriComponentsBuilder ucb) {

        IncidentDto saved =
            incidentService.
                        createIncident(req);

        Long id = saved.id();

        var location = ucb.path("/api/incidents/{id}")
                                    .buildAndExpand(id).toUri();

        return ResponseEntity.created(location).body(saved);
    }

}
