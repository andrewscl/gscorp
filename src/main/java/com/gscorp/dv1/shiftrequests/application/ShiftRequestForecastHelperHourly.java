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

public class ShiftRequestForecastHelperHourly {


    /**
     * Calcula forecast por hora para una fecha dada (en la zona provista).
     * Retorna un Map hour(0-23) -> integer count.
     *
     * Lógica:
     * - Para cada schedule:
     *   - se verifica que la fecha esté dentro de la vigencia del shiftRequest (requestStartDate/requestEndDate).
     *   - se verifica si el dayFrom..dayTo cubre el dayOfWeek de la fecha.
     *   - si aplica, se determina el intervalo horario (startTime,endTime), considerando cruces de medianoche,
     *     y se incrementa en 1 cada hora que tenga intersección con el intervalo del schedule.
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
            if ((df == null || df.isBlank()) && (dt == null || dt.isBlank())) {
                // si no hay restricción por día, asumir aplica todos los días
            } else {
                DayOfWeek dowFrom = parseDayOfWeek(df);
                DayOfWeek dowTo = parseDayOfWeek(dt);
                if (dowFrom != null && dowTo != null) {
                    // comprobar inclusión circular (dowFrom..dowTo)
                    if (!isDayInRangeInclusive(targetDow, dowFrom, dowTo)) {
                        continue; // no aplica este schedule para la fecha
                    }
                } else if (dowFrom != null && dowTo == null) {
                    if (targetDow != dowFrom) continue;
                } else if (dowFrom == null && dowTo != null) {
                    if (targetDow != dowTo) continue;
                }
            }

            // Obtener horarios
            LocalTime start = s.getStartTime();
            LocalTime end = s.getEndTime();
            if (start == null || end == null) continue;

            // Construir intervalos considerando posible cruce de medianoche
            // Representamos [startDateTime, endDateTime) relative a la target date
            ZonedDateTime zStart = ZonedDateTime.of(date, start, zone);
            ZonedDateTime zEnd = ZonedDateTime.of(date, end, zone);
            if (!zEnd.isAfter(zStart)) {
                // si end <= start entendemos que cruza a la siguiente fecha
                zEnd = zEnd.plusDays(1);
            }

            // Para cada hora 0..23 en la day window, comprobar intersección
            for (int h = 0; h < 24; h++) {
                ZonedDateTime hourStart = ZonedDateTime.of(date, LocalTime.of(h, 0), zone);
                ZonedDateTime hourEnd = hourStart.plusHours(1);
                // También considerar hours belonging to next day if schedule crosses midnight:
                // if interval extends into next day, hour h may correspond to next date's hours
                // therefore we must also check hour indices beyond 23: we only aggregate into 0..23 of the original date
                // so we check intersection between [zStart,zEnd) and [hourStart,hourEnd) OR [hourStart +1d,hourEnd+1d) if hour belongs to next day
                boolean intersects = intervalIntersects(zStart, zEnd, hourStart, hourEnd);
                // Additionally, if zEnd > end-of-day and hourStart is on next day, check with hourStart+1day
                if (!intersects) {
                    ZonedDateTime hourStartNext = hourStart.plusDays(1);
                    ZonedDateTime hourEndNext = hourEnd.plusDays(1);
                    intersects = intervalIntersects(zStart, zEnd, hourStartNext, hourEndNext);
                }
                if (intersects) {
                    result.put(h, result.getOrDefault(h, 0) + 1);
                }
            }
        }

        return result;
    }

    private static boolean intervalIntersects(ZonedDateTime aStart, ZonedDateTime aEnd, ZonedDateTime bStart, ZonedDateTime bEnd) {
        return aStart.isBefore(bEnd) && bStart.isBefore(aEnd);
    }

    private static DayOfWeek parseDayOfWeek(String s) {
        if (s == null) return null;
        String t = s.trim().toUpperCase(Locale.ROOT);
        // Try full enum names: MONDAY, TUESDAY, ...
        try {
            return DayOfWeek.valueOf(t);
        } catch (Exception e) { /* ignore */ }
        // Try short names (Mon, Tue, etc.)
        Map<String, DayOfWeek> map = new HashMap<>();
        for (DayOfWeek d : DayOfWeek.values()) {
            map.put(d.getDisplayName(TextStyle.SHORT, Locale.ENGLISH).toUpperCase(Locale.ROOT), d);
            map.put(d.getDisplayName(TextStyle.SHORT, Locale.forLanguageTag("es")).toUpperCase(Locale.ROOT), d); // por si viene "lun", "mar", etc
            map.put(d.name().substring(0,3), d);
        }
        DayOfWeek res = map.get(t.length() > 3 ? t.substring(0,3) : t);
        return res;
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
