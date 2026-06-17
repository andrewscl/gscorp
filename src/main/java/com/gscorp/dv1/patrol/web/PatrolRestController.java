package com.gscorp.dv1.patrol.web;

import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.gscorp.dv1.patrol.application.patrols.PatrolService;
import com.gscorp.dv1.patrol.application.schedules.PatrolScheduleService;
import com.gscorp.dv1.patrol.web.dto.patrols.CreatePatrolRequest;
import com.gscorp.dv1.patrol.web.dto.patrols.PatrolDto;
import com.gscorp.dv1.patrol.web.dto.patrols.UpdatePatrolRequest;
import com.gscorp.dv1.users.application.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/patrols")
@RequiredArgsConstructor
public class PatrolRestController {

    private final PatrolService patrolService;
    private final PatrolScheduleService patrolScheduleService;
    private final UserService userService;

    @PostMapping("/create")
    public ResponseEntity<PatrolDto> createPatrol(
        @Valid @RequestBody CreatePatrolRequest req,
        UriComponentsBuilder ucb,
        Authentication authentication
        ) {

        //Resolver usuario
        Long userId = userService.getUserIdFromAuthentication(authentication);
        if (userId == null) {
            throw new AuthenticationCredentialsNotFoundException("Usuario no autenticado");
        }
        PatrolDto saved = patrolService.savePatrol(req, userId);
        var location = ucb.path("/api/patrols/{externalId}")
                            .buildAndExpand(saved.externalId())
                            .toUri();
        return ResponseEntity.created(location).body(saved);
    }


    @PutMapping("/update/{externalId}")
    public ResponseEntity<PatrolDto> updatePatrol(
        @PathVariable String externalId,
        @Valid @RequestBody UpdatePatrolRequest req,
        Authentication authentication
    ) {

        //Resolver usuario
        Long userId = userService.getUserIdFromAuthentication(authentication);
        if (userId == null) {
            throw new
            AuthenticationCredentialsNotFoundException("Usuario no autenticado");
        }

        PatrolDto updated = patrolService.updatePatrol(externalId, req, userId);

        return ResponseEntity.ok(updated);
    }


    @GetMapping("/patrol-schedules/{siteExternalId}")
    public ResponseEntity<?> getPatrolSchedulesBySiteExternalId (
                        @PathVariable UUID siteExternalId) {

        try {
            return ResponseEntity
                        .ok(patrolScheduleService
                                .getTodaySchedulesBySiteExternalId(siteExternalId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", "Error al buscar PatrolSchedules"));
        }

    }

}
