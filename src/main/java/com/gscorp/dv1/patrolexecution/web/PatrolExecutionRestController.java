package com.gscorp.dv1.patrolexecution.web;

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
import com.gscorp.dv1.users.application.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/patrol-execution")
@RequiredArgsConstructor
public class PatrolExecutionRestController {

    private final UserService userService;
    private final PatrolExecutionService patrolExecutionService;

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PatrolExecutionDto> createSiteSupervisionVisit(
        @Valid @ModelAttribute CreatePatrolExecutionRequest req,
        UriComponentsBuilder ucb,
        Authentication authentication) {

        //Buscar usuario autenticado
        Long userId = userService.getUserIdFromAuthentication(authentication);
        if (userId == null) {
            log.warn("Usuario no autenticado: ", authentication);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "usuario no autenticado.");
        }

        PatrolExecutionDto created =
                patrolExecutionService.createPatrolExecution(req, userId);

        var location = ucb.path("/api/patrol-execution/{id}")
                            .buildAndExpand(created.id()).toUri();

        return ResponseEntity.created(location).body(created);

    }

}
