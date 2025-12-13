package com.gscorp.dv1.attendance.infrastructure;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AttendancePunchRepo extends JpaRepository <AttendancePunch, Long>{

    Optional<AttendancePunch> findFirstByUserIdOrderByTsDesc(Long userId);

    
    List<AttendancePunch> findByUserIdAndTsBetweenOrderByTsAsc(Long userId, OffsetDateTime from, OffsetDateTime to);
    // Para colaborador (por usuario + fecha):
    List<AttendancePunch> findByUserIdAndTsBetweenOrderByTsDesc(Long userId, OffsetDateTime from, OffsetDateTime to);
    // Alternativa admin (global por fecha):
    List<AttendancePunch> findByTsBetweenOrderByTsDesc(OffsetDateTime from, OffsetDateTime to);
    
    /** Proyección tipada para series: alias deben ser 'day' y 'cnt' */
    interface DayCount {
        String getDay();
        Long   getCnt();
    }

    /** Serie diaria: cuenta de marcaciones por día (opcionalmente filtrando por acción y usuario). */
    @Query(value = """
        select to_char(ts::date, 'YYYY-MM-DD') as day, count(*) as cnt
        from attendance_punches
        where ts::date >= :from and ts::date <= :to
        and (:userId is null or user_id = :userId)
        and (:action is null or action = :action)
        group by day
        order by day
    """, nativeQuery = true)
    List<DayCount> countByDay(
        @Param("from") LocalDate from,
        @Param("to")   LocalDate to,
        @Param("action") String action,     // 'IN' | 'OUT' | null (todas)
        @Param("userId") Long userId        // null = todos
    );

        @Query("select count(a) from AttendancePunch a " +
            "where a.site.project.client.id = :clientId " +
            "and a.ts between :from and :to")
        long countByClientIdAndTsBetween(@Param("clientId") Long clientId,
                                        @Param("from") OffsetDateTime from,
                                        @Param("to") OffsetDateTime to);


    /** Proyección para conteo por hora; alias deben ser 'hour' y 'cnt' */
    interface HourlyCount {
        String getHour(); // "00".."23"
        Long   getCnt();
    }

    /**
     * Native query (Postgres): genera series 0..23 y hace LEFT JOIN con conteos agregados por hora.
     * Parámetros:
     *  - :date -> LocalDate (YYYY-MM-DD)
     *  - :tz   -> zona horaria (p.ej. 'America/Santiago') usada con AT TIME ZONE
     *  - :action -> 'IN'|'OUT' o null
     *  - :userId -> Long o null (filtrar por usuario si no es null)
     */
    @Query(value = """
    WITH hours AS (SELECT generate_series(0,23) AS hr)
    SELECT to_char(h.hr, 'FM00') AS hour,
            COALESCE(a.cnt, 0) AS cnt
    FROM hours h
    LEFT JOIN (
        SELECT (EXTRACT(hour FROM (ap.ts AT TIME ZONE :tz)))::int AS hr,
            COUNT(*) AS cnt
        FROM attendance_punches ap
        WHERE ap.ts >= :from
        AND ap.ts <  :to
        AND (:action IS NULL OR ap.action = :action)
        AND (:userId IS NULL OR ap.user_id = :userId)
        GROUP BY hr
    ) a ON a.hr = h.hr
    ORDER BY h.hr
    """, nativeQuery = true)
    List<HourlyCount> findHourlyCountsForRange(
        @Param("from") OffsetDateTime from,
        @Param("to") OffsetDateTime to,
        @Param("tz") String tz,
        @Param("action") String action,
        @Param("userId") Long userId
    );

    /**
     * Cuenta marcaciones cuyo site pertenece a un project que pertenece a uno de los clientIds.
     * Usa a.ts (OffsetDateTime) entre from (inclusive) y to (exclusive).
     * Si action es null, no se filtra por acción.
     */
    @Query("select count(a) " +
           "from AttendancePunch a " +
           "where a.site.project.client.id in :clientIds " +
           "  and a.ts >= :from and a.ts < :to " +
           "  and (:action is null or a.action = :action)")
    long countByClientIdsAndTsBetweenAndAction(
            @Param("clientIds") List<Long> clientIds,
            @Param("from") OffsetDateTime from,
            @Param("to") OffsetDateTime to,
            @Param("action") String action);


    @Query("""
        SELECT
          ap.id            AS id,
          ap.userId        AS userId,
          s.id             AS siteId,
          s.name           AS siteName,
          ap.ts            AS ts,
          ap.lat           AS lat,
          ap.lon           AS lon,
          ap.accuracyM     AS accuracyM,
          ap.action        AS action,
          ap.locationOk    AS locationOk,
          ap.distanceM     AS distanceM,
          ap.deviceInfo    AS deviceInfo,
          ap.ip            AS ip,
          e.id             AS employeeId,
          e.name           AS employeeName,
          e.fatherSurname  AS employeeFatherSurname,
          ap.clientTimezone AS clientTimezone,
          ap.timezoneSource AS timezoneSource,
          ap.createdAt     AS createdAt,
          ap.updatedAt     AS updatedAt
        FROM AttendancePunch ap
        LEFT JOIN ap.site s
        LEFT JOIN s.project p
        LEFT JOIN ap.user u
        LEFT JOIN u.employee e
        WHERE p.client.id IN :clientIds
            AND ap.ts >= :start
            AND ap.ts <  :endExclusive
            AND (:siteId IS NULL OR s.id = :siteId)
            AND (:projectId IS NULL OR p.id = :projectId)
            AND (:action IS NULL OR ap.action = :action)
        """)
    List<AttendancePunchProjection> findByClientIdsAndDateBetween(
        @Param("clientIds") List<Long> clientIds,
        @Param("start") OffsetDateTime start,
        @Param("endExclusive") OffsetDateTime endExclusive,
        @Param("siteId") Long siteId,
        @Param("projectId") Long projectId,
        @Param("action") String action
    );


}
