package com.gscorp.dv1.shiftrequests.web;

import java.net.URI;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import com.gscorp.dv1.clientaccounts.application.ClientAccountService;
import com.gscorp.dv1.clientaccounts.web.dto.ClientAccountDto;
import com.gscorp.dv1.components.ZoneResolver;
import com.gscorp.dv1.components.dto.ZoneResolutionResult;
import com.gscorp.dv1.shiftrequests.application.ShiftRequestForecastHelper;
import com.gscorp.dv1.shiftrequests.application.ShiftRequestForecastHelperHourly;
import com.gscorp.dv1.shiftrequests.application.ShiftRequestService;
import com.gscorp.dv1.shiftrequests.infrastructure.ShiftRequestScheduleProjection;
import com.gscorp.dv1.shiftrequests.infrastructure.ShiftRequestScheduleRepository;
import com.gscorp.dv1.shiftrequests.web.dto.CreateShiftRequest;
import com.gscorp.dv1.shiftrequests.web.dto.ShiftRequestDto;
import com.gscorp.dv1.users.application.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/shift-requests")
@RequiredArgsConstructor
public class ShiftRequestRestController {

    private final ShiftRequestService shiftRequestService;
    private final ShiftRequestScheduleRepository scheduleRepo;
    private final UserService userService;
    private final ClientAccountService clientAccountService;
    private final ZoneResolver zoneResolver;



    @PostMapping("/create")
    public ResponseEntity<ShiftRequestDto> createShiftRequest(
        @jakarta.validation.Valid @RequestBody CreateShiftRequest req,
        Authentication authentication,
        UriComponentsBuilder ucb) {

        // delega en el service que valida permisos y crea la entidad
        ShiftRequestDto dto = shiftRequestService.createShiftRequestForPrincipal(req, authentication);

        Long id = dto != null ? dto.id() : null;

        if (id != null) {
            URI uri = ucb.path("/api/shift-requests/{id}").buildAndExpand(id).toUri();
            return ResponseEntity.created(uri).body(dto);
        } else {
            // si no tenemos id exponible, devolvemos 201 con body sin Location
            return ResponseEntity.status(HttpStatus.CREATED).body(dto);
        }

    }



    @PutMapping("/{id}")
    public ResponseEntity<ShiftRequestDto> editShiftRequest(
        @PathVariable Long id,
        @jakarta.validation.Valid @RequestBody CreateShiftRequest req
    ) {
        Optional<ShiftRequestDto> updatedDtoOpt = shiftRequestService.update(id, req);

        return updatedDtoOpt
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }



    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteShiftRequest(@PathVariable Long id) {
        try {
            boolean deleted = shiftRequestService.deleteShiftRequest(id);

            if (deleted) {
                return ResponseEntity.noContent().build(); // Retorna 204 si se eliminó correctamente
            } else {
                return ResponseEntity.notFound().build(); // Retorna 404 si no existe
            }
        } catch (Exception ex) {
            log.error("Error al eliminar ShiftRequest con ID {}: {}", id, ex.getMessage(), ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "No se pudo eliminar la solicitud de turno.");
        }
    }



    @GetMapping("/sites/{siteId}/accounts")
    public ResponseEntity<List<ClientAccountDto>> getClientAccountsForSite(
                @PathVariable ("siteId") Long siteId,
                Authentication authentication ) {
        
        Long userId = userService.getUserIdFromAuthentication(authentication);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<ClientAccountDto> accounts = clientAccountService.getClientAccountsForSite(siteId, userId);

        return ResponseEntity.ok(accounts);
    }



    @GetMapping("/forecast-series")
    public ResponseEntity<List<Map<String, Object>>> getShiftRequestForecast(
            Authentication authentication,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required=false) Integer days,
            @RequestParam(required = false) String clientTz,
            @RequestParam(required = false) Long siteId
    ){


        String username = (authentication == null) ? "anonymous"
                                : authentication.getName();
        log.info("Incoming /series request user={} from={} to={} days={} tz={}",
                                    username, from, to, days, clientTz);

        try{

            final int DEFAULT_DAYS = 7;
            final int MAX_DAYS = 90;

            Long userId = userService.getUserIdFromAuthentication(authentication);
            if (userId == null) {
                log.warn("Usuario no autenticado (userId null) para auth={}", authentication);
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "usuario no autenticado.");
            }

            ZoneResolutionResult zr = zoneResolver.resolveZone(userId, clientTz);
            ZoneId zone = zr.zoneId();

        // Calcular from/to (igual que visitController)
        LocalDate fromDate;
        LocalDate toDate;
        if (from != null && to != null) {
            fromDate = from;
            toDate = to;
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

        // Enforce max span
        long span = java.time.temporal.ChronoUnit.DAYS.between(fromDate, toDate) + 1;
        if (span > MAX_DAYS) {
            fromDate = toDate.minusDays(MAX_DAYS - 1L);
            log.warn("Requested range too large, truncated a los últimos {} días: from={} to={}", MAX_DAYS, fromDate, toDate);
        }

        // Obtener clientIds asociados al usuario
        List<Long> clientIds = userService.getClientIdsForUser(userId);
        if (clientIds == null || clientIds.isEmpty()) {
            // No hay clientes -> devolver serie con ceros para el rango solicitado
            Map<LocalDate, Integer> emptyMap = new TreeMap<>();
            LocalDate d = fromDate;
            while (!d.isAfter(toDate)) {
                emptyMap.put(d, 0);
                d = d.plusDays(1);
            }
        List<Map<String, Object>> emptySeries = emptyMap.entrySet().stream()
            .map(e -> {
                Map<String, Object> m = new HashMap<>();
                m.put("date", e.getKey().toString());
                m.put("value", e.getValue());
                return m;
            })
            .collect(Collectors.toList());
            return ResponseEntity.ok(emptySeries);
        }

        // Si se pasó siteId, preferimos filtrar por siteId (como override); si no, usar todos los schedules de los clientIds
        List<ShiftRequestScheduleProjection> schedules;
        if (siteId != null) {
            // si quieres respetar también el clientIds puedes comprobar que el site pertenece a uno de los clientIds
            schedules = scheduleRepo.findBySiteId(siteId);
        } else {
            schedules = scheduleRepo.findByClientIds(clientIds);
        }

        // Generar forecast por día (ShiftRequestForecastHelper inicializa el mapa con 0s)
        Map<LocalDate, Integer> forecast = ShiftRequestForecastHelper.forecastByDay(schedules, fromDate, toDate, zone);

        // Transformar a lista ordenada [{ "date": "YYYY-MM-DD", "value": N }, ...]
        List<Map<String, Object>> series = new ArrayList<>(forecast.size());
        for (Map.Entry<LocalDate, Integer> e : forecast.entrySet()) {
            Map<String, Object> point = new HashMap<>();
            point.put("date", e.getKey().toString());
            point.put("value", e.getValue() == null ? 0 : e.getValue());
            series.add(point);
        }

        return ResponseEntity.ok(series);

        } catch (ResponseStatusException rse) {
            throw rse; // rethrow
        } catch (Exception ex) {
            log.error("Error en /forecast: " + ex.getMessage(), ex);
            throw new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR
                              , "Error al obtener forecast de solicitudes de turno.");
        }
    }



    @GetMapping("/forecast-series/hourly")
    public ResponseEntity<List<Map<String, Object>>> getShiftRequestForecastHourly(
            Authentication authentication,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String tz,
            @RequestParam(required = false) Long siteId,
            @RequestParam(required = false) Long projectId
    ) {
        try {
            if (date == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing required parameter: date");
            }

            Long userId = userService.getUserIdFromAuthentication(authentication);
            if (userId == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "usuario no autenticado.");
            }

            // Resolver la zona (requested tz -> user -> system)
            ZoneResolutionResult zr = zoneResolver.resolveZone(userId, tz);
            ZoneId zone = zr.zoneId();

            // Obtener clientIds asociados al usuario (igual que tu otro endpoint)
            List<Long> clientIds = userService.getClientIdsForUser(userId);
            if (clientIds == null || clientIds.isEmpty()) {
                // devolver 24 puntos con cero
                List<Map<String, Object>> empty = new ArrayList<>(24);
                for (int h = 0; h < 24; h++) {
                    Map<String, Object> m = new HashMap<>();
                    m.put("hour", String.format("%02d", h));
                    m.put("value", 0);
                    empty.add(m);
                }
                return ResponseEntity.ok(empty);
            }

            // Obtener schedules: filtrar por siteId si viene, sino por clientIds
            List<ShiftRequestScheduleProjection> schedules;
            if (siteId != null) {
                schedules = scheduleRepo.findBySiteId(siteId);
            } else {
                schedules = scheduleRepo.findByClientIds(clientIds);
            }

            // Llamar al helper que produce forecast por hora para la fecha dada
            Map<Integer, Integer> hourly = ShiftRequestForecastHelperHourly.forecastByHour(schedules, date, zone);

            // Transformar a lista ordenada de 00..23
            List<Map<String, Object>> series = new ArrayList<>(24);
            for (int h = 0; h < 24; h++) {
                Map<String, Object> m = new HashMap<>();
                m.put("hour", String.format("%02d", h));
                m.put("value", hourly.getOrDefault(h, 0));
                series.add(m);
            }

            return ResponseEntity.ok(series);

        } catch (ResponseStatusException rse) {
            throw rse;
        } catch (Exception ex) {
            log.error("Error en /forecast/hourly: " + ex.getMessage(), ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al obtener forecast hourly.");
        }
    }



}   
    
