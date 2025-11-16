package com.gscorp.dv1.patrol.application;

import java.math.BigDecimal;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gscorp.dv1.patrol.infrastructure.PatrolRunRepo;
import com.gscorp.dv1.patrol.web.dto.PatrolHourlyDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

    @Slf4j
    @Service
    @RequiredArgsConstructor
    public class PatrolRunServiceImpl implements PatrolRunService {
        
    private final PatrolRunRepo patrolRunRepo;

    @Override
    @Transactional(readOnly = true)
    public Double compliance(Collection<Long> clientIds, LocalDate from, LocalDate to, ZoneId zone) {
        // defensiva básica
        if (clientIds == null || clientIds.isEmpty()) return null;
        Objects.requireNonNull(from, "from must not be null");
        Objects.requireNonNull(to, "to must not be null");

        ZoneId zoneId = (zone == null) ? ZoneId.of("UTC") : zone;
        ZonedDateTime startZ = from.atStartOfDay(zoneId);
        ZonedDateTime endZ = to.plus(1, ChronoUnit.DAYS).atStartOfDay(zoneId);

        OffsetDateTime fromOffset = startZ.toOffsetDateTime();
        OffsetDateTime toOffset = endZ.toOffsetDateTime();

        BigDecimal bd = patrolRunRepo.complianceForClients(java.util.List.copyOf(clientIds), fromOffset, toOffset);
        return bd == null ? null : bd.doubleValue();
    }

    @Override
    @Transactional(readOnly = true)
    public PatrolRunRepo.HitsSum hitsSum(Collection<Long> clientIds, LocalDate from, LocalDate to, ZoneId zone) {
        if (clientIds == null || clientIds.isEmpty()) {
            return zeroHitsSum();
        }
        Objects.requireNonNull(from, "from must not be null");
        Objects.requireNonNull(to, "to must not be null");

        ZoneId zoneId = (zone == null) ? ZoneId.of("UTC") : zone;
        ZonedDateTime startZ = from.atStartOfDay(zoneId);
        ZonedDateTime endZ = to.plus(1, ChronoUnit.DAYS).atStartOfDay(zoneId);

        OffsetDateTime fromOffset = startZ.toOffsetDateTime();
        OffsetDateTime toOffset = endZ.toOffsetDateTime();

        PatrolRunRepo.HitsSum sum = patrolRunRepo.hitsSumForClients(java.util.List.copyOf(clientIds), fromOffset, toOffset);
        return sum == null ? zeroHitsSum() : sum;
    }


    /**
     * Implementación del método requerido: obtiene conteos horarios por site para los clientIds indicados.
     * Mapea la proyección del repo a PatrolHourlyDto. Para cada site devuelve 24 registros (00..23) con count 0
     * cuando no hay datos en esa hora.
     */
    @Override
    @Transactional(readOnly = true)
    public List<PatrolHourlyDto> getPatrolHourlyCounts(Collection<Long> clientIds, LocalDate date, String tz) {
        if (clientIds == null || clientIds.isEmpty()) return Collections.emptyList();
        Objects.requireNonNull(date, "date must not be null");

        ZoneId zone;
        try {
            zone = (tz == null || tz.isBlank()) ? ZoneId.of("UTC") : ZoneId.of(tz);
        } catch (Exception e) {
            log.warn("Invalid tz '{}', falling back to UTC", tz);
            zone = ZoneId.of("UTC");
        }

        ZonedDateTime startZ = date.atStartOfDay(zone);
        ZonedDateTime endZ = startZ.plus(1, ChronoUnit.DAYS);

        OffsetDateTime from = startZ.toOffsetDateTime();
        OffsetDateTime to = endZ.toOffsetDateTime();

        List<PatrolRunRepo.HourlySiteCount> rows = patrolRunRepo.findHourlySiteCountsForRange(from, to, zone.getId(), List.copyOf(clientIds));
        if (rows == null || rows.isEmpty()) return Collections.emptyList();

        // Agrupar resultados por siteId
        Map<Long, String> siteNames = new HashMap<>();
        Map<Long, Map<String, Long>> countsBySite = new HashMap<>();

        for (PatrolRunRepo.HourlySiteCount r : rows) {
            Long siteId = r.getSiteId();
            String siteName = r.getSiteName() == null ? "" : r.getSiteName();
            String hour = r.getHour() == null ? "00" : r.getHour();
            Long cnt = r.getCnt() == null ? 0L : r.getCnt();

            siteNames.putIfAbsent(siteId, siteName);
            countsBySite.computeIfAbsent(siteId, k -> new HashMap<>()).merge(hour, cnt, Long::sum);
        }

        List<Long> siteIds = countsBySite.keySet().stream().sorted().collect(Collectors.toList());
        List<PatrolHourlyDto> result = new ArrayList<>();

        // Nota: PatrolHourlyDto was defined earlier as (Long patrolId, String patrolName, String hour, long count)
        // Here we map siteId -> patrolId, siteName -> patrolName for compatibility with the DTO used by frontend.
        for (Long sid : siteIds) {
            String name = siteNames.getOrDefault(sid, "");
            Map<String, Long> hourly = countsBySite.get(sid);
            IntStream.range(0, 24).forEach(i -> {
                String hh = String.format("%02d", i);
                long c = hourly.getOrDefault(hh, 0L);
                result.add(new PatrolHourlyDto(sid, name, hh, c));
            });
        }

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public long countByClientIdsAndDate(List<Long> clientIds, LocalDate date, String tz) {
        if (clientIds == null || clientIds.isEmpty()) return 0L;
        if (date == null) return 0L;

        ZoneId zone;
        try {
            zone = (tz == null || tz.isBlank()) ? ZoneId.systemDefault() : ZoneId.of(tz);
        } catch (DateTimeException ex) {
            zone = ZoneId.systemDefault();
        }

        ZonedDateTime startZdt = date.atStartOfDay(zone);
        ZonedDateTime endZdt = startZdt.plusDays(1);

        OffsetDateTime from = startZdt.toOffsetDateTime();
        OffsetDateTime to = endZdt.toOffsetDateTime();

        return patrolRunRepo.countByClientIdsAndStartedAtBetween(clientIds, from, to);
    }

    private PatrolRunRepo.HitsSum zeroHitsSum() {
        return new PatrolRunRepo.HitsSum() {
            @Override
            public Long getCompleted() { return 0L; }
            @Override
            public Long getExpected() { return 0L; }
        };
    }



}
