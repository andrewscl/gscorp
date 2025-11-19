package com.gscorp.dv1.sitesupervisionvisits.web;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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

import com.gscorp.dv1.sites.application.SiteService;
import com.gscorp.dv1.sites.web.dto.SiteDto;
import com.gscorp.dv1.sitesupervisionvisits.application.SiteSupervisionVisitService;
import com.gscorp.dv1.sitesupervisionvisits.web.dto.CreateSiteSupervisionVisitRequest;
import com.gscorp.dv1.sitesupervisionvisits.web.dto.SiteSupVisitPointDto;
import com.gscorp.dv1.sitesupervisionvisits.web.dto.SiteSupervisionVisitDto;
import com.gscorp.dv1.sitesupervisionvisits.web.dto.SiteVisitCountDto;
import com.gscorp.dv1.sitesupervisionvisits.web.dto.SiteVisitHourlyDto;
import com.gscorp.dv1.users.application.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/site-supervision-visits")
@RequiredArgsConstructor
public class SiteSupervisionVisitRestController {

    private final SiteService siteService;
    private final UserService userService;
    private final SiteSupervisionVisitService siteSupervisionVisitService;

    @GetMapping("/sites")
    public List<SiteDto> getSitesApi() {
        return siteService.getAllSites();
    }

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SiteSupervisionVisitDto> createSiteSupervisionVisit(
        @Valid @ModelAttribute CreateSiteSupervisionVisitRequest req,
        UriComponentsBuilder ucb) {

        SiteSupervisionVisitDto saved =
            siteSupervisionVisitService.
                        createSiteSupervisionVisitRequest(req);

        var location = ucb.path("/api/site-supervision-visits/{id}")
                                    .buildAndExpand(saved.id()).toUri();

        return ResponseEntity.created(location).body(saved);
    }

    @GetMapping("/series")
    public List<SiteSupVisitPointDto> SiteSupervisionVisitsByUserSeries(
                    @RequestParam String from,
                    @RequestParam String to,
                    @RequestParam Integer days,
                    @RequestParam String tz,
                    Authentication authentication) {

        final int DEFAULT_DAYS = 7;
        final int MAX_DAYS = 90; // limita para proteger la BD

        Long userId = userService.getUserIdFromAuthentication(authentication);
        if (userId == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "usuario no autenticado.");

        // Resolver zona
        ZoneId zone;
        try {
            zone = (tz == null || tz.isBlank()) ? ZoneId.systemDefault() : ZoneId.of(tz);
        } catch (DateTimeException ex) {
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

        return siteSupervisionVisitService.getVisitsSeriesForUserByDates(userId, fromDate, toDate, zone);
    }

    static class Point {
        public String x; // fecha (ej: "2025-11-02")
        public long y;   // cantidad de visitas
        public Point(String x, long y) { this.x = x; this.y = y; }
    }

    @GetMapping("/kpis")
    public Map<String, Object> getKpis(
                        @RequestParam(required=false) String tz,
                                Authentication authentication) {

        Long userId = userService.getUserIdFromAuthentication(authentication);
        if(userId == null ) {
          throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "usuario no autenticado.");
        }

        List<Long> allowedClientIds = userService.getClientIdsForUser(userId);
        if(allowedClientIds == null || allowedClientIds.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No autorizado ara ver KPIs");
        }

        log.debug("KPI request requesterId={} allowedClientIds={} tz={}", userId, allowedClientIds, tz);

        LocalDate today = LocalDate.now();
        long visitasHoy = siteSupervisionVisitService.countByClientIdsAndDate(allowedClientIds, today, tz);

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

        return siteSupervisionVisitService.getVisitsBySite(clientId, fromOffset, toOffset);
    }

  /**
   * Devuelve conteos por sitio y por hora para la fecha dada (en la tz solicitada).
   * Ejemplo: GET /api/site-supervision-visits/hourly?clientId=1&date=2025-11-14&tz=America/Santiago
   *
   * Si se pasa clientId en query sólo se usará cuando el llamador sea admin (userService.isAdmin(auth)).
   * En caso contrario se usará el clientId obtenido del Authentication vía userService.
   */
  @GetMapping("/hourly")
  public ResponseEntity<List<SiteVisitHourlyDto>> hourly(
      Authentication auth,
      @RequestParam(required = false) List<Long> clientId,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
      @RequestParam(required = false, defaultValue = "UTC") String tz
  ) {

   List<Long> effectiveClientIds;

    boolean isAdmin = userService.isAdmin(auth);

    if (clientId != null && !clientId.isEmpty()) {
      // clientId provided in query
      if (!isAdmin) {
        // ensure the provided ids belong to the caller
        Long callerUserId = userService.getUserIdFromAuthentication(auth);
        List<Long> userClients = userService.getClientIdsForUser(callerUserId);
        if (userClients == null || !userClients.containsAll(clientId)) {
          log.warn("User {} attempted to access clients {} but only has {}", callerUserId, clientId, userClients);
          return ResponseEntity.status(403).build();
        }
      }
      effectiveClientIds = clientId;
      log.debug("Using clientIds from query: {}", effectiveClientIds);
    } else {
      // no clientId param: for non-admin resolve the user's clients; for admin require explicit client(s)
      if (isAdmin) {
        log.warn("Admin requested /hourly without clientId; admin must specify clientId(s)");
        return ResponseEntity.badRequest().build(); // or change to return all clients if desired
      } else {
        Long callerUserId = userService.getUserIdFromAuthentication(auth);
        effectiveClientIds = userService.getClientIdsForUser(callerUserId);
        if (effectiveClientIds == null || effectiveClientIds.isEmpty()) {
          log.warn("No clientIds found for userId={}", callerUserId);
          return ResponseEntity.status(403).build();
        }
        log.debug("Resolved clientIds {} for user {}", effectiveClientIds, callerUserId);
      }
    }

    // Call service (now accepts a collection of client IDs)
    List<SiteVisitHourlyDto> out = siteSupervisionVisitService.getSiteVisitHourlyCounts(effectiveClientIds, date, tz);

    return ResponseEntity.ok(out == null ? Collections.emptyList() : out);
  }

}