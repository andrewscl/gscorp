package com.gscorp.dv1.sitesupervisionvisits.web;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/site-supervision-visits")
@RequiredArgsConstructor
public class SiteSupervisionVisitRestController {

    private final SiteService siteService;

    private final SiteSupervisionVisitService siteSupervisionVisitService;

    @Value("${file.upload-dir}")
    private String uploadDir;
    
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

}