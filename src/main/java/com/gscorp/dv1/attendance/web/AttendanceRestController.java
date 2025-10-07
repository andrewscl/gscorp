package com.gscorp.dv1.attendance.web;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.gscorp.dv1.attendance.application.AttendanceService;

import java.util.Map;

@RestController @RequestMapping("/api/attendance")
public class AttendanceRestController {
  private final AttendanceService svc;
  public AttendanceRestController(AttendanceService svc){ this.svc = svc; }

  @Data public static class PunchReq { @NotNull Double lat; @NotNull Double lon; Double accuracy; }

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

  private static String firstNonBlank(String ...arr){ for (var s:arr) if (s!=null && !s.isBlank()) return s; return null; }
  private Long currentUserId(Authentication auth){ return 1L; } // TODO: id real desde tu SecurityUser/JWT
}
