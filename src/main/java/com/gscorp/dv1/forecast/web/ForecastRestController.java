package com.gscorp.dv1.forecast.web;

import java.util.Collections;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.gscorp.dv1.forecast.application.ForecastService;
import com.gscorp.dv1.forecast.web.dto.ForecastCreateDto;
import com.gscorp.dv1.forecast.web.dto.ForecastRecordDto;
import com.gscorp.dv1.users.application.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/forecasts")
@RequiredArgsConstructor
public class ForecastRestController {
    
    private final ForecastService forecastService;
    private final UserService userService;

    @PostMapping("/create")
    public ResponseEntity<?> createForecast(
        Authentication authentication,
        @Valid @RequestBody ForecastCreateDto req,
        UriComponentsBuilder ucb) {

        Long userId = userService.getUserIdFromAuthentication(authentication);
        if(userId == null) {
            log.warn("Solicitud /api/forecasts/create sin usuario válido en el JWT");
            return ResponseEntity.status(401).body(Collections.singletonMap("error", "unauthenticated"));
        }

        log.debug("Solicitud entrante: /api/forecasts/create userId={} payload={}", userId, req);

        try {
            ForecastRecordDto saved = forecastService.createForecast(req, userId);
            Long id = saved.id();
            var location = ucb.path("/api/forecasts/{id}").buildAndExpand(id).toUri();
            log.debug("Forecast creado id={} by userId={}", id, userId);
            return ResponseEntity.created(location).body(saved);
        } catch (IllegalArgumentException ex) {
            log.warn("Validación inválida al crear forecast: {}", ex.getMessage());
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", ex.getMessage()));
        } catch (Exception ex) {
            log.error("Error inesperado creando forecast", ex);
            return ResponseEntity.status(500).body(Collections.singletonMap("error", "internal_server_error"));
        }

    }
}
