package com.gscorp.dv1.sitesupervisionvisits.web;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.gscorp.dv1.sites.application.SiteService;
import com.gscorp.dv1.sites.web.dto.SiteDto;
import com.gscorp.dv1.sitesupervisionvisits.application.SiteSupervisionVisitService;
import com.gscorp.dv1.sitesupervisionvisits.web.dto.CreateSiteSupervisionVisitRequest;
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
    public List<Point> visitsSeries(
        @RequestParam Long clientId,
        @RequestParam String from,
        @RequestParam String to
    ) {
        // 1. Parsear fechas
        LocalDate fromDate = LocalDate.parse(from);
        LocalDate toDate = LocalDate.parse(to);

        // Convertir rango completo de días a OffsetDateTime
        OffsetDateTime fromOffset = fromDate.atStartOfDay(ZoneOffset.systemDefault()).toOffsetDateTime();
        OffsetDateTime toOffset = toDate.plusDays(1).atStartOfDay(ZoneOffset.systemDefault()).toOffsetDateTime().minusNanos(1);
        
        // 2. Consultar visitas del cliente entre fechas
        List<SiteSupervisionVisitDto> visits = siteSupervisionVisitService
            .findByClientIdAndDateBetween(clientId, fromOffset, toOffset);

        // 3. Agrupar por día y contar
        Map<LocalDate, Long> grouped = visits.stream()
            .collect(Collectors.groupingBy(
                dto -> dto.visitDateTime().toLocalDate(),
                Collectors.counting()
            ));

        // 4. Preparar datos para Echarts
        List<Point> series = new ArrayList<>();
        LocalDate d = fromDate;
        while (!d.isAfter(toDate)) {
            long y = grouped.getOrDefault(d, 0L);
            series.add(new Point(d.toString(), y));
            d = d.plusDays(1);
        }
        return series;
    }

    static class Point {
        public String x; // fecha (ej: "2025-11-02")
        public long y;   // cantidad de visitas
        public Point(String x, long y) { this.x = x; this.y = y; }
    }

    @GetMapping("/kpis")
    public Map<String, Object> getKpis(@RequestParam Long clientId) {
        LocalDate today = LocalDate.now();
        long visitasHoy = siteSupervisionVisitService.countByClientIdAndDate(clientId, today);

        return Map.of(
            "visitasHoy", visitasHoy
            // agrega los otros KPIs aquí si los necesitas
        );
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