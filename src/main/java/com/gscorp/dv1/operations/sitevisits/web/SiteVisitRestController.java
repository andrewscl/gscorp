package com.gscorp.dv1.operations.sitevisits.web;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import com.gscorp.dv1.admin.clients.application.ClientService;
import com.gscorp.dv1.components.ZoneResolver;
import com.gscorp.dv1.components.dto.ZoneResolutionResult;
import com.gscorp.dv1.config.security.SecurityUser;
import com.gscorp.dv1.operations.sites.application.SiteService;
import com.gscorp.dv1.operations.sites.web.dto.SiteDto;
import com.gscorp.dv1.operations.sitevisits.application.SiteVisitService;
import com.gscorp.dv1.operations.sitevisits.web.dto.CreateSiteSupervisionVisit;
import com.gscorp.dv1.operations.sitevisits.web.dto.SiteVisitCountDto;
import com.gscorp.dv1.operations.sitevisits.web.dto.SiteVisitDto;
import com.gscorp.dv1.operations.sitevisits.web.dto.SiteVisitHourlyDto;
import com.gscorp.dv1.operations.sitevisits.web.dto.SiteVisitPointDto;
import com.gscorp.dv1.users.application.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/site-supervision-visits")
@RequiredArgsConstructor
public class SiteVisitRestController {

    private final SiteService siteService;
    private final ClientService clientService;
    private final UserService userService;
    private final SiteVisitService siteVisitService;
    private final ZoneResolver zoneResolver;

    @GetMapping("/sites")
    public List<SiteDto> getSitesApi() {
        return siteService.getAllSites();
    }



    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SiteVisitDto> createSiteSupervisionVisit(
        @Valid @ModelAttribute CreateSiteSupervisionVisit req,
        UriComponentsBuilder ucb,
        Authentication authentication) {

        //Buscar usuario autenticado
        Long userId = userService.getUserIdFromAuthentication(authentication);
        if (userId == null) {
            log.warn("Usuario no autenticado para /forecast-series auth={}", authentication);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "usuario no autenticado.");
        }

        Object principal = authentication.getPrincipal();
        SecurityUser securityUser = (SecurityUser) principal;
        UUID externalId = securityUser.getUser().getExternalId();

        SiteVisitDto saved =
            siteVisitService.
                        createSiteSupervisionVisitRequest(req, externalId);

        var location = ucb.path("/api/site-supervision-visits/{id}")
                                    .buildAndExpand(saved.id()).toUri();

        return ResponseEntity.created(location).body(saved);
    }



    @GetMapping("/series")
    public List<SiteVisitPointDto> SiteSupervisionVisitsByUserSeries(
                    @RequestParam(required=false) String from,
                    @RequestParam(required=false) String to,
                    @RequestParam(required=false) Integer days,
                    @RequestParam(required=false) String tz,
                    Authentication authentication) {

        Object principal = authentication.getPrincipal();
        SecurityUser securityUser = (SecurityUser) principal;
        UUID externalId = securityUser.getUser().getExternalId();

    try{

        final int DEFAULT_DAYS = 7;
        final int MAX_DAYS = 90;

        Long userId = userService.getUserIdFromAuthentication(authentication);
        if (userId == null) {
            log.warn("Usuario no autenticado (userId null) para auth={}", authentication);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "usuario no autenticado.");
        }

        // Resolver zona con fallback
        ZoneId zone;
        try {
            zone = (tz == null || tz.isBlank()) ? ZoneId.systemDefault() : ZoneId.of(tz);
        } catch (DateTimeException ex) {
            log.warn("tz inválida '{}', usando default: {}", tz, ZoneId.systemDefault(), ex);
            zone = ZoneId.systemDefault();
        }

        // Calcular fromDate/toDate:
        LocalDate fromDate;
        LocalDate toDate;
        if (from != null && to != null) {
            fromDate = LocalDate.parse(from);
            toDate = LocalDate.parse(to);
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

        log.debug("Series request userId={} from={} to={} days={} tz={}", userId, fromDate, toDate, days, zone);
        return siteVisitService.getVisitsSeriesForUserByDates(externalId, fromDate, toDate, zone);

    } catch (Exception ex) {
        log.error("Error en /series: " + ex.getMessage(), ex);
        throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR
                          , "Error al obtener series de visitas.");
    }

  }



    @GetMapping("/kpis")
    public Map<String, Object> getKpis(
                        @RequestParam(required=false) String tz,
                                Authentication authentication) {

        Long userId = userService.getUserIdFromAuthentication(authentication);
        if(userId == null ) {
          throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "usuario no autenticado.");
        }

        Object principal = authentication.getPrincipal();
        SecurityUser securityUser = (SecurityUser) principal;
        UUID externalId = securityUser.getUser().getExternalId();

        List<Long> allowedClientIds = clientService.getClientIdsByUserExternalId(externalId);
        if(allowedClientIds == null || allowedClientIds.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No autorizado ara ver KPIs");
        }

        log.debug("KPI request requesterId={} allowedClientIds={} tz={}", userId, allowedClientIds, tz);

        LocalDate today = LocalDate.now();
        long visitasHoy = siteVisitService.countByClientIdsAndDate(allowedClientIds, today, tz);

        return Map.of("visitasHoy", visitasHoy);
    }

    @GetMapping("/series-by-site")
    public List<SiteVisitCountDto> visitsBySite(
        @RequestParam Long clientId,
        @RequestParam String from,
        @RequestParam String to
    ) {
        LocalDate fromDate = LocalDate.parse(from);
        LocalDate toDate = LocalDate.parse(to);
        OffsetDateTime fromOffset = fromDate.atStartOfDay(ZoneOffset.systemDefault()).toOffsetDateTime();
        OffsetDateTime toOffset = toDate.plusDays(1).atStartOfDay(ZoneOffset.systemDefault()).toOffsetDateTime().minusNanos(1);

        return siteVisitService.getVisitsBySite(clientId, fromOffset, toOffset);
    }


    @GetMapping("/hourly-aggregated")
    public ResponseEntity<List<SiteVisitHourlyDto>> hourlyAggregatedForCaller(
        Authentication auth,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
        @RequestParam(required = false, defaultValue = "UTC") String tz
        ) {
        Long callerUserId = userService.getUserIdFromAuthentication(auth);
        if (callerUserId == null) {
        return ResponseEntity.status(403).build();
        }

        Object principal = auth.getPrincipal();
        SecurityUser securityUser = (SecurityUser) principal;
        UUID externalId = securityUser.getUser().getExternalId();

        // Resolver zona con ZoneResolver (prioriza requested tz, luego user profile, luego sistema)
        ZoneResolutionResult zr = zoneResolver.resolveZone(externalId, tz);
        ZoneId resolvedZone = zr.zoneId();
        String resolvedTzId = resolvedZone.getId();
        log.debug("Resolved zone for userId={} requestedTz='{}' => {} (source={})",
            callerUserId, tz, resolvedTzId, zr.source());

        // Si date no fue provista, calcular "hoy" en la zona resuelta
        if (date == null) {
        date = LocalDate.now(resolvedZone);
        log.debug("No date provided - using date={} based on resolved zone {}", date, resolvedTzId);
        }

        List<SiteVisitHourlyDto> series = siteVisitService
            .getVisitsSeriesForUserByDateByVisitHourlyAgregated(externalId, date, tz);

        return ResponseEntity.ok(series == null ? Collections.emptyList() : series);
    }

}