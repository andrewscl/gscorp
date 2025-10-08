package com.gscorp.dv1.dashboard.api;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.format.annotation.DateTimeFormat;
import static org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.gscorp.dv1.dashboard.api.dto.KpiResponse;
import com.gscorp.dv1.dashboard.api.dto.SeriesPoint;
import com.gscorp.dv1.dashboard.application.ClientDashboardService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/client-dashboard")
@RequiredArgsConstructor
public class ClientDashboardController {

  private final ClientDashboardService svc;

  /* ====== Endpoints ====== */

  /** KPIs agregados del cliente en el rango indicado (por defecto últimos 30 días). */
  @GetMapping("/kpis")
  @PreAuthorize("hasAnyRole('ADMINISTRATOR','CLIENT')")
  public ResponseEntity<KpiResponse> kpis(
      Authentication auth,
      @RequestParam(required = false) Long clientId,
      @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate from,
      @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate to
  ) {
    Range r = normalizeRange(from, to);
    Long id = resolveClientId(auth, clientId);
    return ResponseEntity.ok(svc.kpis(id, r.from(), r.to()));
  }

  /** Serie diaria de ENTRADAS (asistencia) del cliente. */
  @GetMapping("/attendance/series")
  @PreAuthorize("hasAnyRole('ADMINISTRATOR','CLIENT')")
  public ResponseEntity<List<SeriesPoint>> attendance(
      Authentication auth,
      @RequestParam(required = false) Long clientId,
      @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate from,
      @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate to
  ) {
    Range r = normalizeRange(from, to);
    Long id = resolveClientId(auth, clientId);
    return ResponseEntity.ok(svc.attendanceSeries(id, r.from(), r.to()));
  }

  /** Serie diaria de incidentes del cliente. */
  @GetMapping("/incidents/series")
  @PreAuthorize("hasAnyRole('ADMINISTRATOR','CLIENT')")
  public ResponseEntity<List<SeriesPoint>> incidents(
      Authentication auth,
      @RequestParam(required = false) Long clientId,
      @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate from,
      @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate to
  ) {
    Range r = normalizeRange(from, to);
    Long id = resolveClientId(auth, clientId);
    return ResponseEntity.ok(svc.incidentsSeries(id, r.from(), r.to()));
  }

  /* ====== Helpers ====== */

  /** Si es CLIENT, toma su clientId del principal; si es ADMIN, usa el parámetro (obligatorio). */
  private Long resolveClientId(Authentication auth, Long clientIdParam) {
    if (isClient(auth)) {
      return clientIdFromPrincipal(auth)
          .orElseThrow(() -> new IllegalStateException("clientId no presente en el token/usuario"));
    }
    // ADMIN: debe indicar clientId
    if (clientIdParam == null) {
      throw new IllegalArgumentException("clientId es requerido para ADMIN");
    }
    return clientIdParam;
  }

  /** Default: últimos 30 días si no se entregan; valida from <= to. */
  private Range normalizeRange(LocalDate from, LocalDate to) {
    LocalDate today = LocalDate.now();
    LocalDate f = (from != null) ? from : today.minusDays(29);
    LocalDate t = (to   != null) ? to   : today;
    if (t.isBefore(f)) throw new IllegalArgumentException("El parámetro 'to' no puede ser anterior a 'from'.");
    return new Range(f, t);
  }

  private boolean isClient(Authentication auth) {
    return auth != null && auth.getAuthorities().stream()
        .anyMatch(a -> "ROLE_CLIENT".equals(a.getAuthority()));
  }

  private Optional<Long> clientIdFromPrincipal(Authentication auth) {
    // TODO: adapta esto a tu modelo de seguridad real.
    // Ejemplos:
    // - Si usas JWT: ((JwtAuthenticationToken)auth).getToken().getClaim("clientId")
    // - Si usas UserDetails custom: ((SecurityUser)auth.getPrincipal()).getClientId()
    return Optional.of(1L); // <-- placeholder
  }

  private record Range(LocalDate from, LocalDate to) {}
}
