package com.gscorp.dv1.patrol.web;

import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.gscorp.dv1.exceptions.ResourceNotFoundException;
import com.gscorp.dv1.patrol.application.PatrolService;
import com.gscorp.dv1.patrol.infrastructure.Patrol;
import com.gscorp.dv1.patrol.web.dto.CreatePatrolRequest;
import com.gscorp.dv1.patrol.web.dto.PatrolDto;
import com.gscorp.dv1.sites.application.SiteService;
import com.gscorp.dv1.sites.infrastructure.Site;
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
    private final SiteService siteService;
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

        //Resolver site
        Long siteId = req.siteId();
        Optional<Site> site = siteService.findById(siteId);

        if (site.isEmpty()) {
            throw new ResourceNotFoundException("Site not found: " + siteId);
        }

        //Construir entidad
        var entity = Patrol.builder()
            .site(site.get())
            .name(req.name())
            .dayFrom(req.dayFrom())
            .dayTo(req.dayTo())
            .build();

        PatrolDto saved = patrolService.savePatrol(entity);

        var location = ucb.path("/api/patrols/{id}").buildAndExpand(saved.id()).toUri();

        return ResponseEntity.created(location).body(saved);
    }

}
