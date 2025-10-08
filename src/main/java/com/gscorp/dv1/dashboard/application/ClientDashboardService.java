package com.gscorp.dv1.dashboard.application;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.gscorp.dv1.dashboard.api.dto.KpiResponse;
import com.gscorp.dv1.dashboard.api.dto.SeriesPoint;
import com.gscorp.dv1.dashboard.infrastructure.ClientDashboardRepo;

import lombok.RequiredArgsConstructor;

@Service @RequiredArgsConstructor
public class ClientDashboardService {
    
  private final ClientDashboardRepo repo;

  public List<SeriesPoint> attendanceSeries(Long clientId, LocalDate from, LocalDate to) {
    var rows = repo.attendanceInByDay(clientId, from, to);
    var map = rows.stream().collect(Collectors.toMap(
      r -> (String) r[0], r -> ((Number) r[1]).longValue()
    ));
    var out = new ArrayList<SeriesPoint>();
    for (var d = from; !d.isAfter(to); d = d.plusDays(1)) {
      var key = d.toString();
      out.add(new SeriesPoint(key, map.getOrDefault(key, 0L)));
    }
    return out;
  }

  public List<SeriesPoint> incidentsSeries(Long clientId, LocalDate from, LocalDate to) {
    var rows = repo.incidentsByDay(clientId, from, to);
    var map = rows.stream().collect(Collectors.toMap(
      r -> (String) r[0], r -> ((Number) r[1]).longValue()
    ));
    var out = new ArrayList<SeriesPoint>();
    for (var d = from; !d.isAfter(to); d = d.plusDays(1)) {
      var key = d.toString();
      out.add(new SeriesPoint(key, map.getOrDefault(key, 0L)));
    }
    return out;
  }

  public KpiResponse kpis(Long clientId, LocalDate from, LocalDate to) {
    var compl = Optional.ofNullable(repo.patrolCompliance(clientId, from, to)).orElse(0d);
    var incidentsTotal = incidentsSeries(clientId, from, to).stream().mapToLong(SeriesPoint::y).sum();

    // MVP: attendanceRate = 0 (o calcula un proxy si tienes programados)
    double attendanceRate = 0.0;

    return new KpiResponse(attendanceRate, compl, incidentsTotal);
  }

}
