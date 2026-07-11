package com.gscorp.dv1.operations.shiftrequests.application;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.gscorp.dv1.enums.DayOfWeek;
import com.gscorp.dv1.operations.shiftrequests.infrastructure.projections.ShiftRequestScheduleProjection;

public final class ShiftRequestForecastHelper {


    private ShiftRequestForecastHelper() {}

    /**
     * Construye el conjunto de DayOfWeek entre from..to inclusive.
     * Si no se puede parsear (son nulos), devuelve EnumSet.allOf(DayOfWeek.class) como fallback conservador.
     */
    public static Set<DayOfWeek> buildDaySet(DayOfWeek from, DayOfWeek to) {
        // Fallback: si alguno de los dos extremos no está definido, retornamos todos los días
        if (from == null || to == null) {
            return EnumSet.allOf(DayOfWeek.class);
        }
        
        Set<DayOfWeek> set = EnumSet.noneOf(DayOfWeek.class);
        
        // Extraemos tus números de día (Lunes = 1 ... Domingo = 7)
        int cur = from.getDayNumber(); 
        int end = to.getDayNumber();
        
        while (true) {
            // Buscamos tu constante correspondiente al número actual usando tu método estático
            set.add(DayOfWeek.fromDayNumber(cur));
            
            if (cur == end) break;
            
            // Avanzamos al día siguiente con comportamiento circular (del 7 pasa al 1)
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
                if (allowed.contains(DayOfWeek.fromJavaTime(cur.getDayOfWeek()))) {
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
                if (allowed.contains(DayOfWeek.fromJavaTime(cur.getDayOfWeek()))) {
                    ZonedDateTime z = ZonedDateTime.of(cur, startTime, zone);
                    map.compute(z, (kk, vv) -> (vv == null ? 0 : vv) + positions);
                }
                cur = cur.plusDays(1);
            }
        }
        return map;
    }
}
