package com.gscorp.dv1.forecast.web;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import com.gscorp.dv1.components.ZoneResolver;
import com.gscorp.dv1.enums.ForecastMetric;
import com.gscorp.dv1.forecast.application.ForecastService;
import com.gscorp.dv1.forecast.web.dto.ForecastCreateDto;
import com.gscorp.dv1.forecast.web.dto.ForecastPointDto;
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
    private final ZoneResolver zoneResolver;


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


    @GetMapping("/forecast-series")
    public List<ForecastPointDto> forecastSeries(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) Integer days,
            @RequestParam(required = false) String tz,
            @RequestParam(required = false) ForecastMetric metric,
            @RequestParam(required = false) Long siteId,
            @RequestParam(required = false) Long projectId,
            Authentication authentication) {

        final int DEFAULT_DAYS = 7;
        final int MAX_DAYS = 90;

        String username = (authentication == null) ? "anonymous" : authentication.getName();
        log.info("Incoming /forecast-series request user={} from={} to={} days={} tz={} metric={} siteId={} projectId={}",
                            username, from, to, days, tz, metric, siteId, projectId);

        Long userId = userService.getUserIdFromAuthentication(authentication);
        if (userId == null) {
            log.warn("Usuario no autenticado para /forecast-series auth={}", authentication);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "usuario no autenticado.");
        }

        // Resolver zona con ZoneResolver (requested tz -> user preference -> system default)
        var zr = zoneResolver.resolveZone(userId, tz);
        ZoneId zone = zr.zoneId();

        LocalDate fromDate;
        LocalDate toDate;
        if (from != null && to != null) {
            try {
                fromDate = LocalDate.parse(from);
                toDate = LocalDate.parse(to);
            } catch (DateTimeException ex) {
                log.warn("Fechas inválidas from='{}' to='{}'", from, to, ex);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Formato de fecha inválido. Use YYYY-MM-DD.");
            }
        } else {
            int d = (days == null) ? DEFAULT_DAYS : days;
            if (d < 1) d = 1;
            if (d > MAX_DAYS) d = MAX_DAYS;
            toDate = LocalDate.now(zone);
            fromDate = toDate.minusDays(d - 1);
        }

        // Normalizar orden
        if (fromDate.isAfter(toDate)) {
            LocalDate tmp = fromDate;
            fromDate = toDate;
            toDate = tmp;
        }

        log.debug("Forecast series userId={} from={} to={} tz={} (resolvedSource={})",
                userId, fromDate, toDate, zone, zr.source());

        try {
            return forecastService.getForecastSeriesForUserByDates(
                                    userId, fromDate, toDate, zone, metric, siteId, projectId);
        } catch (Exception ex) {
            log.error("Error obteniendo forecast series", ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al obtener series de forecast.");
        }
    }


}
