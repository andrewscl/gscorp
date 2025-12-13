package com.gscorp.dv1.attendance.web;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.gscorp.dv1.attendance.application.AttendanceService;
import com.gscorp.dv1.attendance.web.dto.HourlyCountDto;
import com.gscorp.dv1.attendance.web.dto.AttendancePunchPointDto;
import com.gscorp.dv1.components.ZoneResolver;
import com.gscorp.dv1.users.application.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceSeriesController {

  private final UserService userService;
  private final AttendanceService attendanceService;
  private final ZoneResolver zoneResolver;

  @GetMapping("/series")
  public ResponseEntity<List<AttendancePunchPointDto>> series(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
        @RequestParam(required = false) Integer days,
        @RequestParam(required = false) String tz,
        @RequestParam(required = false) String action, // "IN" | "OUT" | null
        @RequestParam(required = false) Long siteId,
        @RequestParam(required = false) Long projectId,
      Authentication authentication
    ) {

        final int DEFAULT_DAYS = 7;
        final int MAX_DAYS = 90;

        Long userId = userService.getUserIdFromAuthentication(authentication);
        if (userId == null) {
              log.warn("Usuario no autenticado para /attendance-series auth={}", authentication);
              throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "usuario no autenticado.");
        }

        // Resolver zona con ZoneResolver (requested tz -> user preference -> system default)
        var zr = zoneResolver.resolveZone(userId, tz);
        ZoneId zone = zr.zoneId();

        LocalDate fromDate;
        LocalDate toDate;
        if (from != null && to != null) {
            fromDate = from;
            toDate = to;
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

        String normalizedAction = normalizeAction(action);
        if (action != null && normalizedAction == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "action inválida. Valores aceptados: IN, OUT");
        }

        log.debug("Attendance series request userId={} from={} to={} tz={} action={} siteId={} projectId={} (resolvedSource={})",
        userId, fromDate, toDate, zone, normalizedAction, siteId, projectId, zr.source());

        try{
          List<AttendancePunchPointDto> series =
               attendanceService.getAttendanceSeriesForUserByDates(
                          userId, fromDate, toDate, zone, normalizedAction, siteId, projectId);         
          

        return ResponseEntity.ok()
                .header("X-Resolved-timezone", zone.toString())
                .body(series);

        } catch (ResponseStatusException e) {
          throw e;
        } catch (Exception ex) {
          log.error("Error en AttendanceSeriesController.series: {}", ex.getMessage(), ex);
          throw new ResponseStatusException(
              HttpStatus.INTERNAL_SERVER_ERROR,
              "Error obteniendo series de asistencia.");
          }

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
      @RequestParam(required = false) String action,
      @RequestParam(required = false) Long userId
  ) {

   // Si se pasó userId como query param y el llamador es admin, lo usamos.
    Long effectiveUserId;
    if (userId != null && userService.isAdmin(auth)) {
      effectiveUserId = userId;
    } else {
      // Reutilizamos el UserService para extraer el id del Authentication
      effectiveUserId = userService.getUserIdFromAuthentication(auth);
    }

    List<HourlyCountDto> out = attendanceService.getHourlyCounts(date, tz, action, effectiveUserId);

    return ResponseEntity.ok(out);
  }


  private static String normalizeAction(String a) {
    if (a == null) return null;
    var t = a.trim();
    return t.isEmpty() ? null : t.toUpperCase(); // "IN"/"OUT"
  }


}
