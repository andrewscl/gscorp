package com.gscorp.dv1.operations.patrolexecution.web;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.gscorp.dv1.config.security.SecurityUser;
import com.gscorp.dv1.operations.patrolexecution.application.patrolsexecution.PatrolExecutionService;
import com.gscorp.dv1.operations.patrolexecution.web.dto.patrolsexecution.PatrolExecutionDto;
import com.gscorp.dv1.operations.patrolexecution.web.dto.patrolsexecution.StartPatrolExecutionRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/patrol-executions")
@RequiredArgsConstructor
public class PatrolExecutionRestController {

    private final PatrolExecutionService patrolExecutionService;

    @PostMapping("/start/{patrolScheduleExternalId}")
    public ResponseEntity<PatrolExecutionDto> startExecution(
                    @PathVariable UUID patrolScheduleExternalId,
                    @RequestBody StartPatrolExecutionRequest request,
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
                patrolExecutionService.startPatrolExecution(request, patrolScheduleExternalId, userExternalId);

        return ResponseEntity.status(HttpStatus.CREATED).body(created);

    }

}
