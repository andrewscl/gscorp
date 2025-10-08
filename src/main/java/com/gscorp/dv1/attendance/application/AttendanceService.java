package com.gscorp.dv1.attendance.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gscorp.dv1.attendance.infrastructure.AttendancePunch;
import com.gscorp.dv1.attendance.infrastructure.AttendancePunchRepo;

import java.time.*;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AttendanceService {

  private final AttendancePunchRepo repo;

  // Config rápida (muévela a BD)
  private static final double SITE_LAT = -33.45;
  private static final double SITE_LON = -70.666;
  private static final double MAX_DIST_METERS = 250.0;
  private static final long   MIN_GAP_SECONDS = 60;

  /** COMANDO: registrar marcación IN/OUT alternada */
  @Transactional
  public AttendancePunch punch(Long userId, double lat, double lon, Double acc, String ip, String ua) {
    var now  = OffsetDateTime.now();
    var last = repo.findFirstByUserIdOrderByTsDesc(userId).orElse(null);

    if (last != null && Duration.between(last.getTs(), now).getSeconds() < MIN_GAP_SECONDS) {
      return last; // anti doble click
    }

    double dist = haversineMeters(lat, lon, SITE_LAT, SITE_LON);
    boolean ok  = dist <= MAX_DIST_METERS;

    // Guardamos en MAYÚSCULAS para consistencia con consultas
    String nextAction = (last == null || "OUT".equalsIgnoreCase(last.getAction())) ? "IN" : "OUT";

    var p = AttendancePunch.builder()
        .userId(userId)
        .ts(now)
        .lat(lat).lon(lon).accuracyM(acc)
        .action(nextAction)
        .locationOk(ok).distanceM(dist)
        .ip(ip).deviceInfo(ua)
        .build();

    return repo.save(p);
  }

  /** ÚLTIMA marcación del usuario */
  @Transactional(readOnly = true)
  public Optional<AttendancePunch> lastPunch(Long userId) {
    return repo.findFirstByUserIdOrderByTsDesc(userId);
  }

  /** SERIE por usuario y rango (opcional: acción "IN"/"OUT") */
  @Transactional(readOnly = true)
  public List<AttendancePunchRepo.DayCount> seriesByUser(Long userId, LocalDate from, LocalDate to, String action) {
    return repo.countByDay(from, to, action, userId);
  }

  /** Lista de marcaciones del usuario en el rango (detalle) */
  @Transactional(readOnly = true)
  public List<AttendancePunch> listForUser(Long userId, LocalDate from, LocalDate to, ZoneId zone) {
    var fromTs = from.atStartOfDay(zone).toOffsetDateTime();
    var toTs   = to.plusDays(1).atStartOfDay(zone).minusNanos(1).toOffsetDateTime();
    return repo.findByUserIdAndTsBetweenOrderByTsDesc(userId, fromTs, toTs);
  }

  /** Utilidad geodésica */
  public static double haversineMeters(double lat1,double lon1,double lat2,double lon2){
    double R=6371000, dLat=Math.toRadians(lat2-lat1), dLon=Math.toRadians(lon2-lon1);
    double a=Math.sin(dLat/2)*Math.sin(dLat/2)
           + Math.cos(Math.toRadians(lat1))*Math.cos(Math.toRadians(lat2))
           * Math.sin(dLon/2)*Math.sin(dLon/2);
    return 2*R*Math.atan2(Math.sqrt(a),Math.sqrt(1-a));
  }
}
