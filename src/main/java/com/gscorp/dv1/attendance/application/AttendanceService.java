package com.gscorp.dv1.attendance.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.gscorp.dv1.attendance.infrastructure.AttendancePunch;
import com.gscorp.dv1.attendance.infrastructure.AttendancePunchRepo;

import java.time.*;

@Service @RequiredArgsConstructor
public class AttendanceService {
  private final AttendancePunchRepo repo;

  // Config rápida (muévelo a BD o yml si quieres)
  private static final double SITE_LAT = -33.45;      // ejemplo
  private static final double SITE_LON = -70.666;
  private static final double MAX_DIST_METERS = 250.0;
  private static final long   MIN_GAP_SECONDS = 60;   // anti doble click

  public AttendancePunch punch(Long userId, double lat, double lon, Double acc, String ip, String ua){
    var last = repo.findFirstByUserIdOrderByTsDesc(userId).orElse(null);
    if (last != null && Duration.between(last.getTs(), OffsetDateTime.now()).getSeconds() < MIN_GAP_SECONDS) {
      return last; // debouncing simple
    }

    double dist = haversineMeters(lat, lon, SITE_LAT, SITE_LON);
    boolean ok  = dist <= MAX_DIST_METERS;

    String action = (last == null || "out".equalsIgnoreCase(last.getAction())) ? "in" : "out";

    var p = AttendancePunch.builder()
      .userId(userId)
      .lat(lat).lon(lon).accuracyM(acc)
      .action(action).locationOk(ok).distanceM(dist)
      .ip(ip).deviceInfo(ua)
      .build();

    return repo.save(p);
  }

  public static double haversineMeters(double lat1,double lon1,double lat2,double lon2){
    double R=6371000, dLat=Math.toRadians(lat2-lat1), dLon=Math.toRadians(lon2-lon1);
    double a=Math.sin(dLat/2)*Math.sin(dLat/2) + Math.cos(Math.toRadians(lat1))*Math.cos(Math.toRadians(lat2))*Math.sin(dLon/2)*Math.sin(dLon/2);
    return 2*R*Math.atan2(Math.sqrt(a),Math.sqrt(1-a));
  }
}
