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

}
