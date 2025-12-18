package com.gscorp.dv1.shiftrequests.application;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.TextStyle;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import com.gscorp.dv1.shiftrequests.infrastructure.ShiftRequestScheduleProjection;


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

        DayOfWeek targetDow = date.getDayOfWeek();

        for (ShiftRequestScheduleProjection s : schedules) {
            // validar vigencia del shiftRequest que contiene este schedule
            LocalDate reqStart = s.getRequestStartDate();
            LocalDate reqEnd = s.getRequestEndDate();
            if (reqStart != null && date.isBefore(reqStart)) continue;
            if (reqEnd != null && date.isAfter(reqEnd)) continue;

            // dayFrom/dayTo: intentar parsear a DayOfWeek
            String df = s.getDayFrom();
            String dt = s.getDayTo();
            if (!((df == null || df.isBlank()) && (dt == null || dt.isBlank()))) {
                DayOfWeek dowFrom = parseDayOfWeek(df);
                DayOfWeek dowTo = parseDayOfWeek(dt);
                if (dowFrom != null && dowTo != null) {
                    if (!isDayInRangeInclusive(targetDow, dowFrom, dowTo)) {
                        continue; // no aplica este schedule para la fecha
                    }
                } else if (dowFrom != null && dowTo == null) {
                    if (targetDow != dowFrom) continue;
                } else if (dowFrom == null && dowTo != null) {
                    if (targetDow != dowTo) continue;
                }
            }

            // Obtener hora de inicio (startTime). Si no existe, ignorar.
            LocalTime start = s.getStartTime();
            if (start == null) continue;

            // Convertir la hora de inicio a ZonedDateTime usando la fecha objetivo y la zona.
            // Esto respeta DST y asegura la hora local correcta.
            ZonedDateTime zStart = ZonedDateTime.of(date, start, zone);
            int startHour = zStart.getHour();

            // Incrementar la cuenta para la hora de inicio
            result.put(startHour, result.getOrDefault(startHour, 0) + 1);
        }

        return result;
    }

    private static DayOfWeek parseDayOfWeek(String s) {
        if (s == null) return null;
        String t = s.trim().toUpperCase(Locale.ROOT);
        try {
            return DayOfWeek.valueOf(t);
        } catch (Exception e) { /* ignore */ }
        Map<String, DayOfWeek> map = new HashMap<>();
        for (DayOfWeek d : DayOfWeek.values()) {
            map.put(d.getDisplayName(TextStyle.SHORT, Locale.ENGLISH).toUpperCase(Locale.ROOT), d);
            map.put(d.getDisplayName(TextStyle.SHORT, new Locale("es")).toUpperCase(Locale.ROOT), d);
            map.put(d.name().substring(0,3), d);
        }
        String key = t.length() > 3 ? t.substring(0,3) : t;
        return map.get(key);
    }

    private static boolean isDayInRangeInclusive(DayOfWeek target, DayOfWeek from, DayOfWeek to) {
        if (from == null || to == null) return true;
        int f = from.getValue(); // MONDAY=1 .. SUNDAY=7
        int t = to.getValue();
        int x = target.getValue();
        if (f <= t) {
            return x >= f && x <= t;
        } else {
            // rango circular (p. ej. Fri..Mon)
            return x >= f || x <= t;
        }
    }
}