package com.gscorp.dv1.patrolexecution.web;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import com.gscorp.dv1.patrolexecution.application.patrolsexecution.PatrolExecutionService;
import com.gscorp.dv1.patrolexecution.web.dto.patrolsexecution.CreatePatrolExecutionRequest;
import com.gscorp.dv1.patrolexecution.web.dto.patrolsexecution.PatrolExecutionDto;
import com.gscorp.dv1.security.SecurityUser;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/patrol-execution")
@RequiredArgsConstructor
public class PatrolExecutionRestController {

    private final PatrolExecutionService patrolExecutionService;

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PatrolExecutionDto> createPatrolExecution(
        @Valid @ModelAttribute CreatePatrolExecutionRequest req,
        UUID patrolExternalId,
        UriComponentsBuilder ucb,
        Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("Intento de acceso no autenticado");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado.");
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof SecurityUser securityUser)) { // 🔒 Defensivo: Evita un ClassCastException sorpresa
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Estructura de seguridad no reconocida.");
        }
        
        UUID userExternalId = securityUser.getUser().getExternalId();

        PatrolExecutionDto created =
                patrolExecutionService.createPatrolExecution(req, patrolExternalId, userExternalId);

        var location = ucb.path("/api/patrol-execution/{id}")
                            .buildAndExpand(created.id()).toUri();

        return ResponseEntity.created(location).body(created);

    }

}
