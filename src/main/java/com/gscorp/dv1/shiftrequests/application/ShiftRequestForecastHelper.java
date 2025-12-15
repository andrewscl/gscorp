package com.gscorp.dv1.shiftrequests.application;

import java.text.Normalizer;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.gscorp.dv1.shiftrequests.infrastructure.ShiftRequestScheduleProjection;

public final class ShiftRequestForecastHelper {


    private static final Map<String, DayOfWeek> SPANISH_DAY_MAP;
    static {
        Map<String, DayOfWeek> m = new HashMap<>();
        m.put("LUNES", DayOfWeek.MONDAY);
        m.put("MARTES", DayOfWeek.TUESDAY);
        m.put("MIERCOLES", DayOfWeek.WEDNESDAY);
        m.put("MIÉRCOLES", DayOfWeek.WEDNESDAY);
        m.put("MIERCOLES", DayOfWeek.WEDNESDAY);
        m.put("JUEVES", DayOfWeek.THURSDAY);
        m.put("VIERNES", DayOfWeek.FRIDAY);
        m.put("SABADO", DayOfWeek.SATURDAY);
        m.put("SÁBADO", DayOfWeek.SATURDAY);
        m.put("DOMINGO", DayOfWeek.SUNDAY);
        SPANISH_DAY_MAP = Collections.unmodifiableMap(m);
    }

    private ShiftRequestForecastHelper() {}

    private static String normalizeDayKey(String s) {
        if (s == null) return null;
        String t = s.trim().toUpperCase(Locale.ROOT);
        // eliminar diacríticos/acentos
        t = Normalizer.normalize(t, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        // también eliminar caracteres no-alfanuméricos sobrantes
        t = t.replaceAll("[^A-Z]", "");
        return t;
    }

    private static DayOfWeek parseSpanishDay(String raw) {
        if (raw == null) return null;
        DayOfWeek dow = SPANISH_DAY_MAP.get(raw.trim().toUpperCase(Locale.ROOT));
        if (dow != null) return dow;
        String key = normalizeDayKey(raw);
        return SPANISH_DAY_MAP.get(key);
    }

    /**
     * Construye el conjunto de DayOfWeek entre dayFrom..dayTo inclusive.
     * Si no se puede parsear, devuelve EnumSet.allOf(DayOfWeek.class) como fallback conservador.
     */
    public static Set<DayOfWeek> buildDaySet(String dayFrom, String dayTo) {
        DayOfWeek df = parseSpanishDay(dayFrom);
        DayOfWeek dt = parseSpanishDay(dayTo);
        if (df == null || dt == null) {
            return EnumSet.allOf(DayOfWeek.class); // si no sabemos, aceptar todos
        }
        Set<DayOfWeek> set = EnumSet.noneOf(DayOfWeek.class);
        int cur = df.getValue(); // 1..7
        int end = dt.getValue();
        while (true) {
            set.add(DayOfWeek.of(cur));
            if (cur == end) break;
            cur = cur % 7 + 1;
        }
        return set;
    }

    /**
     * Genera forecast por día (inclusive) contando 1 por cada schedule aplicable.
     * Si en el futuro quieres multiplicidad, añade getPositions() en la proyección y úsala.
     *
     * @param schedules schedules a considerar
     * @param fromInclusive fecha desde (inclusive)
     * @param toInclusive fecha hasta (inclusive)
     * @param zone zona del cliente (para conversiones si hiciera falta)
     * @return TreeMap ordenado por fecha
     */
    public static Map<LocalDate, Integer> forecastByDay(
            List<? extends ShiftRequestScheduleProjection> schedules,
            LocalDate fromInclusive,
            LocalDate toInclusive,
            ZoneId zone
    ) {
        if (fromInclusive == null || toInclusive == null || fromInclusive.isAfter(toInclusive)) {
            return Collections.emptyMap();
        }

        // inicializar mapa con 0s
        Map<LocalDate, Integer> map = new TreeMap<>();
        LocalDate d = fromInclusive;
        while (!d.isAfter(toInclusive)) {
            map.put(d, 0);
            d = d.plusDays(1);
        }

        if (schedules == null || schedules.isEmpty()) return map;

        // fragmento relevante dentro de ShiftForecastHelper.forecastByDay
        for (ShiftRequestScheduleProjection sc : schedules) {
            Set<DayOfWeek> allowed = buildDaySet(sc.getDayFrom(), sc.getDayTo());
            int positions = 1;

            // obtener vigencia del ShiftRequest (puede ser null -> sin restricción)
            LocalDate reqStart = sc.getRequestStartDate();
            LocalDate reqEnd = sc.getRequestEndDate();

            // calcular el rango efectivo sobre el cual este schedule aplica:
            LocalDate effFrom = (reqStart == null) ? fromInclusive : (reqStart.isAfter(fromInclusive) ? reqStart : fromInclusive);
            LocalDate effTo   = (reqEnd == null)   ? toInclusive   : (reqEnd.isBefore(toInclusive) ? reqEnd : toInclusive);

            if (effFrom.isAfter(effTo)) {
                // este schedule no aplica en el rango solicitado
                continue;
            }

            LocalDate cur = effFrom;
            while (!cur.isAfter(effTo)) {
                if (allowed.contains(cur.getDayOfWeek())) {
                    map.compute(cur, (kk, vv) -> (vv == null ? 0 : vv) + positions);
                }
                cur = cur.plusDays(1);
            }
        }

        return map;
    }

    /**
     * (Opcional) genera forecast por slot (ZonedDateTime inicio -> count). Se puede usar
     * para gráficos más detallados por hora.
     */
    public static Map<ZonedDateTime, Integer> forecastBySlot(
            List<? extends ShiftRequestScheduleProjection> schedules,
            LocalDate fromInclusive,
            LocalDate toInclusive,
            ZoneId zone
    ) {
        Map<ZonedDateTime, Integer> map = new TreeMap<>();
        if (fromInclusive == null || toInclusive == null || fromInclusive.isAfter(toInclusive)) return map;
        if (schedules == null || schedules.isEmpty()) return map;

        for (ShiftRequestScheduleProjection sc : schedules) {
            Set<DayOfWeek> allowed = buildDaySet(sc.getDayFrom(), sc.getDayTo());
            LocalTime startTime = sc.getStartTime();
            int positions = 1;
            if (startTime == null) continue;

            LocalDate cur = fromInclusive;
            while (!cur.isAfter(toInclusive)) {
                if (allowed.contains(cur.getDayOfWeek())) {
                    ZonedDateTime z = ZonedDateTime.of(cur, startTime, zone);
                    map.compute(z, (kk, vv) -> (vv == null ? 0 : vv) + positions);
                }
                cur = cur.plusDays(1);
            }
        }
        return map;
    }
}
