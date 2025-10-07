package com.gscorp.dv1.attendance.web;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.gscorp.dv1.attendance.infrastructure.AttendancePunchRepo;
import com.gscorp.dv1.attendance.web.dto.SeriesAttendancePunch;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceSeriesController {

  private final AttendancePunchRepo repo;

  @GetMapping("/series")
  public ResponseEntity<List<SeriesAttendancePunch>> series(
      Authentication auth,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
      @RequestParam(required = false) String action // "IN" | "OUT" | null
  ) {
    // SUGERENCIA: para empleados, filtra por el userId del token
    Long userId = currentUserId(auth); // implementa esto con tu SecurityUser/JWT

    var rows = repo.countByDay(from, to, action, userId); // cada row: [String day, BigInteger cnt]
    Map<String, Long> map = rows.stream().collect(Collectors.toMap(
        r -> (String) r[0],
        r -> ((Number) r[1]).longValue()
    ));

    // Rellenar días faltantes para que el gráfico no “salte”
    var out = new ArrayList<SeriesAttendancePunch>();
    LocalDate d = from;
    while (!d.isAfter(to)) {
      String key = d.toString(); // YYYY-MM-DD
      out.add(new SeriesAttendancePunch(key, map.getOrDefault(key, 0L)));
      d = d.plusDays(1);
    }
    return ResponseEntity.ok(out);
  }

  private Long currentUserId(Authentication auth) {
    // TODO: devuelve el id real del usuario autenticado (p.ej. ((SecurityUser)auth.getPrincipal()).getId())
    return 1L;
  }
}
