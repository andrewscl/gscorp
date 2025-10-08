package com.gscorp.dv1.dashboard.api;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gscorp.dv1.dashboard.api.dto.KpiResponse;
import com.gscorp.dv1.dashboard.api.dto.SeriesPoint;
import com.gscorp.dv1.dashboard.application.ClientDashboardService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/client-dashboard")
@RequiredArgsConstructor
public class ClientDashboardController {

  private final ClientDashboardService svc;

  // Obtén clientId desde el token si el rol es CLIENT; admite param si ADMIN
  private Long resolveClientId(Authentication auth, Long clientIdParam) {
    // TODO: si auth tiene rol CLIENT, extrae su clientId asociado; si ADMIN usa clientIdParam
    return clientIdParam != null ? clientIdParam : 1L;
  }

  @GetMapping("/kpis")
  public ResponseEntity<KpiResponse> kpis(
      Authentication auth,
      @RequestParam Long clientId,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
  ){
    var id = resolveClientId(auth, clientId);
    return ResponseEntity.ok(svc.kpis(id, from, to));
  }

  @GetMapping("/attendance/series")
  public ResponseEntity<List<SeriesPoint>> attendance(
      Authentication auth,
      @RequestParam Long clientId,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
  ){
    var id = resolveClientId(auth, clientId);
    return ResponseEntity.ok(svc.attendanceSeries(id, from, to));
  }

  @GetMapping("/incidents/series")
  public ResponseEntity<List<SeriesPoint>> incidents(
      Authentication auth,
      @RequestParam Long clientId,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
  ){
    var id = resolveClientId(auth, clientId);
    return ResponseEntity.ok(svc.incidentsSeries(id, from, to));
  }

}
