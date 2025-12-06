package com.gscorp.dv1.attendance.web;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import com.gscorp.dv1.attendance.application.AttendanceService;
import com.gscorp.dv1.attendance.web.dto.AttendancePunchDto;
import com.gscorp.dv1.attendance.web.dto.CreateAttendancePunchRequest;
import com.gscorp.dv1.sites.application.SiteService;
import com.gscorp.dv1.sites.web.dto.SiteDto;
import com.gscorp.dv1.users.application.UserService;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceRestController {

  private final SiteService siteService;
  private final UserService userService;
  private final AttendanceService attendanceService;


  //Registrar asistencia
  @PostMapping("/punch")
  public ResponseEntity<?> punch(
              @RequestBody CreateAttendancePunchRequest in,
              Authentication authentication,
              UriComponentsBuilder ucb,
              @RequestHeader(value="User-Agent", required=false) String ua,
              @RequestHeader(value="X-Forwarded-For", required=false) String xff,
              @RequestHeader(value="CF-Connecting-IP", required=false) String cfIp,
              @RequestHeader(value="X-Real-IP", required=false) String xri) {

      Long userId = userService.getUserIdFromAuthentication(authentication);
            if (userId == null) {
              // no autenticado: redirigir al login o devolver error según tu política
              return ResponseEntity.status(401).build();
      }

      String ip = firstNonBlank(cfIp, xff, xri, "0.0.0.0");
      if (in.getIp() == null) in.setIp(ip);
      if (in.getDeviceInfo() == null) in.setDeviceInfo(ua);

      AttendancePunchDto saved = attendanceService.createPunch(in, userId);

      URI location = ucb.path("/api/attendance/punch/{id}")
          .buildAndExpand(saved.id())
          .toUri();

      return ResponseEntity.created(location).body(saved);
  }



  // CONSULTAR ÚLTIMA MARCACIÓN DEL USUARIO
  @GetMapping("/last-punch")
  public ResponseEntity<?> lastPunch(Authentication authentication) {

      Long userId = userService.getUserIdFromAuthentication(authentication);
            if (userId == null) {
              // no autenticado: redirigir al login o devolver error según tu política
              return ResponseEntity.status(401).build();
      }

    var lastOpt = attendanceService.lastPunch(userId);

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


  @GetMapping("/sites")
  @ResponseBody
  public List<SiteDto> getSitesApi() {
    return siteService.getAllSites();
  }

}
