package com.gscorp.dv1.attendance.web;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.gscorp.dv1.attendance.infrastructure.AttendancePunchRepo;
import com.gscorp.dv1.attendance.web.dto.HourlyCountDto;
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
    Long userId = currentUserId(auth); // ajusta según tu seguridad
    String normalizedAction = normalizeAction(action);

    // Ahora devuelve List<AttendancePunchRepo.DayCount>
    var rows = repo.countByDay(from, to, normalizedAction, userId);

    // Mapeo usando getters de la proyección
    Map<String, Long> map = rows.stream().collect(Collectors.toMap(
        AttendancePunchRepo.DayCount::getDay,
        dc -> Optional.ofNullable(dc.getCnt()).orElse(0L)
    ));

    // Rellenar días faltantes
    var out = new ArrayList<SeriesAttendancePunch>();
    for (LocalDate d = from; !d.isAfter(to); d = d.plusDays(1)) {
      String key = d.toString(); // YYYY-MM-DD
      out.add(new SeriesAttendancePunch(key, map.getOrDefault(key, 0L)));
    }
    return ResponseEntity.ok(out);
  }


  /**
   * Devuelve 24 filas con hour "00".."23" y count por esa hora (según tz).
   * Ejemplo: GET /api/attendance/hourly?date=2025-11-14&tz=America/Santiago&action=IN
   */
  @GetMapping("/hourly")
  public ResponseEntity<List<HourlyCountDto>> hourly(
      Authentication auth,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
      @RequestParam(required = false, defaultValue = "UTC") String tz,
      @RequestParam(required = false) String action
  ) {
    Long userId = currentUserId(auth); // ajusta según tu seguridad, puede ser null para todos
    List<AttendancePunchRepo.HourlyCount> rows = repo.findHourlyCountsForDate(date, tz, normalizeAction(action), userId);

    // Mapear proyección a DTO
    List<HourlyCountDto> out = rows.stream()
        .map(r -> new HourlyCountDto(r.getHour(), r.getCnt() == null ? 0L : r.getCnt()))
        .toList();

    return ResponseEntity.ok(out);
  }


  private static String normalizeAction(String a) {
    if (a == null) return null;
    var t = a.trim();
    return t.isEmpty() ? null : t.toUpperCase(); // "IN"/"OUT"
  }

  private Long currentUserId(Authentication auth) {
    // TODO: retorna el id real del usuario autenticado
    return 1L;
  }



  
}
