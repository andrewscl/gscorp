package com.gscorp.dv1.configuration.hrdocuments.web;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.gscorp.dv1.config.security.SecurityUser;
import com.gscorp.dv1.configuration.hrdocuments.application.HrDocumentTypeService;
import com.gscorp.dv1.configuration.hrdocuments.web.dto.CreateHrDocumentType;
import com.gscorp.dv1.configuration.hrdocuments.web.dto.HrDocumentTypeDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/hr-document-types")
@RequiredArgsConstructor
public class HrDocumentTypeRestController {

    private final HrDocumentTypeService hrDocumentTypeService;

    @PostMapping("/create")
    public ResponseEntity <HrDocumentTypeDto> createHrDocumentType (
        @Valid @RequestBody CreateHrDocumentType req,
        UriComponentsBuilder ucb,
        @AuthenticationPrincipal SecurityUser securityUser){
        if (securityUser == null) {
            throw new AuthenticationCredentialsNotFoundException("Usuario no autenticado");
        }

        HrDocumentTypeDto saved =
                    hrDocumentTypeService.createHrDocumentType(req, securityUser);

        var location = ucb.path("/api/hr-document-types/{externalId}")
                            .buildAndExpand(saved.externalId())
                            .toUri();

        return ResponseEntity.created(location).body(saved);
    }

}
