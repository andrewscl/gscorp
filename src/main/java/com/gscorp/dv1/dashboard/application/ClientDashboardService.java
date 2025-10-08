// src/main/java/com/gscorp/dv1/dashboard/application/ClientDashboardService.java
package com.gscorp.dv1.dashboard.application;

import com.gscorp.dv1.dashboard.api.dto.SeriesPoint;
import com.gscorp.dv1.dashboard.infrastructure.ClientDashboardRepo;
import com.gscorp.dv1.dashboard.infrastructure.ClientDashboardRepo.DayCount;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClientDashboardService {

    private final ClientDashboardRepo repo;

    public List<SeriesPoint> attendanceInSeries(Long clientId, LocalDate from, LocalDate to) {
        List<DayCount> rows = repo.attendanceInByDay(clientId, from, to);
        return fillGaps(from, to, toMap(rows));
    }

    public Double patrolCompliance(Long clientId, LocalDate from, LocalDate to) {
        return Optional.ofNullable(repo.patrolCompliance(clientId, from, to)).orElse(0d);
    }

    public List<SeriesPoint> incidentsSeries(Long clientId, LocalDate from, LocalDate to) {
        List<DayCount> rows = repo.incidentsByDay(clientId, from, to);
        return fillGaps(from, to, toMap(rows));
    }

    /* helpers */
    private static Map<String, Long> toMap(List<DayCount> rows) {
        return rows.stream().collect(Collectors.toMap(DayCount::getDay, DayCount::getCnt));
    }

    private static List<SeriesPoint> fillGaps(LocalDate from, LocalDate to, Map<String, Long> map) {
        List<SeriesPoint> out = new ArrayList<>();
        for (LocalDate d = from; !d.isAfter(to); d = d.plusDays(1)) {
            String key = d.toString();
            out.add(new SeriesPoint(key, map.getOrDefault(key, 0L)));
        }
        return out;
    }
}
