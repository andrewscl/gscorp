package com.gscorp.dv1.operations.shiftrequests.application;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.gscorp.dv1.enums.DayOfWeek;
import com.gscorp.dv1.operations.shiftrequests.infrastructure.projections.ShiftRequestScheduleProjection;
import com.gscorp.dv1.operations.shiftrequests.infrastructure.projections.statistics.ProjectSiteShiftRequestSchedulesProjection;
import com.gscorp.dv1.operations.shiftrequests.web.dto.statistics.HourlyComplianceMetrics;


/**
 * Helper para generar forecast por hora (00..23) contando TURNOS por hora de INICIO.
 * Cada schedule que aplica a la fecha incrementa en 1 la hora correspondiente a su startTime.
 */
public class ShiftRequestForecastHelperHourly {

    /**
     * Retorna Map hour(0-23) -> integer count de turnos cuya hora de inicio cae en esa hora (según zone).
     */
    public static Map<Integer, Integer> forecastByHour(List<ShiftRequestScheduleProjection> schedules, LocalDate date, ZoneId zone) {
        Map<Integer, Integer> result = new TreeMap<>();
        for (int h = 0; h < 24; h++) result.put(h, 0);
        if (schedules == null || schedules.isEmpty()) return result;
        DayOfWeek targetDow = DayOfWeek.fromJavaTime(date.getDayOfWeek());
        for (ShiftRequestScheduleProjection s : schedules) {
            // validar vigencia del shiftRequest que contiene este schedule
            LocalDate reqStart = s.getRequestStartDate();
            LocalDate reqEnd = s.getRequestEndDate();
            if (reqStart != null && date.isBefore(reqStart)) continue;
            if (reqEnd != null && date.isAfter(reqEnd)) continue;

                DayOfWeek dowFrom = s.getDayFrom();
                DayOfWeek dowTo = s.getDayTo();
                if (dowFrom != null && dowTo != null) {
                    if (!isDayInRangeInclusive(targetDow, dowFrom, dowTo)) {
                        continue; // no aplica este schedule para la fecha
                    }
                } else if (dowFrom != null && dowTo == null) {
                    if (targetDow != dowFrom) continue;
                } else if (dowFrom == null && dowTo != null) {
                    if (targetDow != dowTo) continue;
                }
            LocalTime start = s.getStartTime();
            if (start == null) continue;
            ZonedDateTime zStart = ZonedDateTime.of(date, start, zone);
            int startHour = zStart.getHour();
            result.put(startHour, result.getOrDefault(startHour, 0) + 1);
        }
        return result;
    }



    private static boolean isDayInRangeInclusive(DayOfWeek target, DayOfWeek from, DayOfWeek to) {
        if (from == null || to == null) return true;
        int f = from.getDayNumber();
        int t = to.getDayNumber();
        int x = target.getDayNumber();
        if (f <= t) {
            return x >= f && x <= t;
        } else {
            // rango circular (p. ej. Fri..Mon)
            return x >= f || x <= t;
        }
    }


    public static Map<String, HourlyComplianceMetrics> forecastSpecificHours(
            List<ProjectSiteShiftRequestSchedulesProjection> schedules,
            LocalDate date,
            ZoneId zone,
            Map<Integer, Long> attendanceCountByHour) { 

        int[] targetHours = {0, 7, 8, 9, 14, 15, 20, 23};

        Map<Integer, long[]> counterMap = new HashMap<>();
        for (int h: targetHours) {
            counterMap.put(h, new long[]{0L, 0L});
        }

        if(schedules != null && !schedules.isEmpty()) {
            DayOfWeek targetDow = DayOfWeek.fromJavaTime(date.getDayOfWeek());

            for(ProjectSiteShiftRequestSchedulesProjection s : schedules) {
                // Validar vigencia del shiftRequest
                LocalDate reqStart = s.getRequestStartDate();
                LocalDate reqEnd = s.getRequestEndDate();
                if(reqStart != null && date.isBefore(reqStart)) continue;
                if(reqEnd != null && date.isAfter(reqEnd)) continue;

                    DayOfWeek dowFrom = s.getDayFrom();
                    DayOfWeek dowTo = s.getDayTo();
                    if (dowFrom != null && dowTo != null) {
                        if(!isDayInRangeInclusive(targetDow, dowFrom, dowTo)) continue;
                    } else if (dowFrom != null && targetDow != dowFrom) {
                        continue;
                    } else if (dowTo != null && targetDow != dowTo) {
                        continue;
                    }

                LocalTime start = s.getStartTime();
                if (start == null) continue;
                ZonedDateTime zStart = ZonedDateTime.of(date, start, zone);
                int startHour = zStart.getHour();
                if(counterMap.containsKey(startHour)) {
                    long[] counts = counterMap.get(startHour);
                    counts[0]++;
                }
            }
        }

        Map<String, HourlyComplianceMetrics> result = new LinkedHashMap<>();
        for(int h : targetHours) {
            long[] counts = counterMap.get(h);

            long attendances = attendanceCountByHour.getOrDefault(h, 0L);
            counts[1] = attendances;

            String label = String.format("%02d:00", h);
            result.put(label, new HourlyComplianceMetrics(counts[0], counts[1]));
        }
        return result;
    }


}