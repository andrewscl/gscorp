package com.gscorp.dv1.forecast.web;

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

@RestController
@RequestMapping("/api/forecasts")
@RequiredArgsConstructor
public class ForecastRestController {
    
    private final ForecastService forecastService;
    private final UserService userService;

    @PostMapping("/create")
    public ResponseEntity<ForecastRecordDto> createForecast(
        Authentication authentication,
        @Valid @RequestBody ForecastCreateDto req,
        UriComponentsBuilder ucb) {

        Long userId = userService.getUserIdFromAuthentication(authentication);
        if(userId == null) {
            // no autenticado: redirigir al login o devolver error según tu política
            return ResponseEntity.status(401).build();
        }

        ForecastRecordDto saved =
            forecastService.createForecast(req, userId);

        Long id = saved.id();

        var location = ucb.path("/api/forecasts/{id}")
                                    .buildAndExpand(id).toUri();

        return ResponseEntity.created(location).body(saved);
    }
}
