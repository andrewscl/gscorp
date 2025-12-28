// src/main/java/com/gscorp/dv1/dashboard/application/ClientDashboardService.java
package com.gscorp.dv1.dashboard.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.gscorp.dv1.dashboard.api.dto.KpiResponse;
import com.gscorp.dv1.dashboard.api.dto.SeriesPoint;
import com.gscorp.dv1.dashboard.infrastructure.ClientDashboardRepo;
import com.gscorp.dv1.dashboard.infrastructure.ClientDashboardRepo.DayCount;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClientDashboardService {

    private final ClientDashboardRepo repo;

    /* ======= Public API usado por el Controller ======= */

    /** KPIs agregados del cliente en el rango dado. */
    public KpiResponse kpis(Long clientId, LocalDate from, LocalDate to) {
        // Asistencia (entradas por día)
        List<DayCount> attRows = repo.attendanceInByDay(clientId, from, to);
        long entries = attRows.stream().mapToLong(DayCount::getCnt).sum();

        // Aún no tenemos expected (dotación planificada) → null, por lo tanto rate = null
        Long expected = null;
        Double rate = (expected != null && expected > 0) ? (entries * 1.0 / expected) : null;

        // Patrullas: tenemos compliance global (0..1). No tenemos hits agregados aún → null
        Double compliance = Optional.ofNullable(repo.patrolCompliance(clientId, from, to)).orElse(0d);

        // Incidentes: total del periodo (sumatoria de la serie diaria)
        List<DayCount> incRows = repo.incidentsByDay(clientId, from, to);
        long incidentsTotal = incRows.stream().mapToLong(DayCount::getCnt).sum();

        return new KpiResponse(
            clientId,
            from, to,
            new KpiResponse.AttendanceKpi(
                rate,          // rate (0..1) o null si no hay expected
                entries,       // entradas reales
                expected,      // expected (null por ahora)
                null,          // delta (comparación con periodo previo) → aún no calculamos
                null           // deltaPct → aún no calculamos
            ),
            new KpiResponse.PatrolKpi(
                compliance,    // 0..1
                null,          // completedHits → agregar query si lo necesitas
                null,          // expectedHits  → agregar query si lo necesitas
                null,          // delta
                null           // deltaPct
            ),
            new KpiResponse.IncidentKpi(
                incidentsTotal,
                null,          // critical
                null,          // open
                null,          // closed
                null           // meanPerDay
            )
        );
    }

    /** Alias para el Controller: serie de ENTRADAS por día. */
    public List<SeriesPoint> attendanceSeries(Long clientId, LocalDate from, LocalDate to) {
        return attendanceInSeries(clientId, from, to);
    }

    /** Serie diaria de ENTRADAS (IN). */
    public List<SeriesPoint> attendanceInSeries(Long clientId, LocalDate from, LocalDate to) {
        List<DayCount> rows = repo.attendanceInByDay(clientId, from, to);
        return fillGaps(from, to, toMap(rows));
    }

    /** Cumplimiento de patrullas (0..1). */
    public Double patrolCompliance(Long clientId, LocalDate from, LocalDate to) {
        return Optional.ofNullable(repo.patrolCompliance(clientId, from, to)).orElse(0d);
    }

    /** Serie diaria de incidentes. */
    public List<SeriesPoint> incidentsSeries(Long clientId, LocalDate from, LocalDate to) {
        List<DayCount> rows = repo.incidentsByDay(clientId, from, to);
        return fillGaps(from, to, toMap(rows));
    }

    /* ======= Helpers ======= */

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
