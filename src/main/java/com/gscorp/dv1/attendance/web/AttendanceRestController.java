package com.gscorp.dv1.attendance.web;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.gscorp.dv1.attendance.application.AttendanceService;
import com.gscorp.dv1.sites.application.SiteService;
import com.gscorp.dv1.sites.web.dto.SiteDto;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceRestController {

  private final SiteService siteService;

  private final AttendanceService svc;


  @Data public static class PunchReq { @NotNull Double lat; @NotNull Double lon; Double accuracy; }

  //Registrar asistencia
  @PostMapping("/punch")
  public ResponseEntity<?> punch(@RequestBody PunchReq in, Authentication auth,
                                 @RequestHeader(value="User-Agent", required=false) String ua,
                                 @RequestHeader(value="X-Forwarded-For", required=false) String xff,
                                 @RequestHeader(value="CF-Connecting-IP", required=false) String cfIp,
                                 @RequestHeader(value="X-Real-IP", required=false) String xri) {
    Long userId = currentUserId(auth);
    String ip = firstNonBlank(cfIp, xff, xri, "0.0.0.0");

    var saved = svc.punch(userId, in.lat, in.lon, in.accuracy, ip, ua);
    return ResponseEntity.ok(Map.of(
      "ts", saved.getTs().toString(),
      "action", saved.getAction(),
      "locationOk", saved.getLocationOk(),
      "distanceMeters", Math.round(saved.getDistanceM()==null?0:saved.getDistanceM())
    ));
  }

  // CONSULTAR ÚLTIMA MARCACIÓN DEL USUARIO
  @GetMapping("/last-punch")
  public ResponseEntity<?> lastPunch(Authentication auth) {
    Long userId = currentUserId(auth);
    var lastOpt = svc.lastPunch(userId);
    if (lastOpt.isEmpty()) {
      // No hay marcación aún
      return ResponseEntity.ok(Map.of());
    }
    var last = lastOpt.get();
    // Puedes retornar solo la acción, o extender la info según requiera el frontend
    return ResponseEntity.ok(Map.of(
      "action", last.getAction(),
      "ts", last.getTs().toString()
    ));
  }

  private static String firstNonBlank(String ...arr){ for (var s:arr) if (s!=null && !s.isBlank()) return s; return null; }
  private Long currentUserId(Authentication auth){ return 1L; } // TODO: id real desde tu SecurityUser/JWT

  @GetMapping("/sites")
  @ResponseBody
  public List<SiteDto> getSitesApi() {
    return siteService.getAllSites();
  }

}
