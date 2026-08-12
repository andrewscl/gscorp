package com.gscorp.dv1.operations.shifts.infrastructure;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.gscorp.dv1.enums.ShiftStatus;
import com.gscorp.dv1.operations.shiftrequests.infrastructure.ShiftRequest;
import com.gscorp.dv1.operations.shifts.infrastructure.projections.ShiftProjection;
import com.gscorp.dv1.operations.shifts.infrastructure.projections.ShiftsCountLast24HoursProjection;

@Repository
public interface ShiftRepository extends JpaRepository<Shift, Long>{

    List<Shift> findBySiteIdAndStartTsBetween(
                                Long siteId, OffsetDateTime from, OffsetDateTime to);

    Optional<Shift> findFirstByShiftRequestExternalIdOrderByShiftDateDesc(UUID externalId);

    @Query(
        value = """
        SELECT
        s.id            AS  id,
        s.externalId    AS  externalId,
        s.shiftDate     AS  shiftDate,
        s.startTs       AS  startTs,
        s.endTs         AS  endTs,
        s.status        AS  status
        FROM Shift s
        JOIN s.shiftRequest sr
        WHERE sr.externalId = :shiftRequestExternalId
        ORDER BY s.shiftDate DESC, s.startTs DESC
    """,
    countQuery = """
        SELECT COUNT(s.id) 
        FROM Shift s 
        JOIN s.shiftRequest sr 
        WHERE sr.externalId = :shiftRequestExternalId
    """
    )
    Page<ShiftProjection> findLastByShiftRequestExternalId(
                @Param("shiftRequestExternalId") UUID shiftRequestExternalId,
                Pageable pageable
                );

    @Query( value = """
        SELECT
            COUNT(sh.id)    AS totalShifts,
            sh.startTs      AS startTs  
        FROM Shift sh
        LEFT JOIN sh.site s
        LEFT JOIN s.project p
        WHERE p.client.id IN :clientIds
            AND sh.startTs >= :since
            AND sh.startTs <= :until
        GROUP BY sh.startTs
        """)
    List<ShiftsCountLast24HoursProjection> getShiftsCountLast24Hours (
            @Param("clientIds") List<Long> clientIds,
            @Param("since") OffsetDateTime since,
            @Param("until") OffsetDateTime until
    );

    @Query ("""
        SELECT s
        FROM Shift s
        WHERE s.shiftRequest = :shiftRequest
        AND s.shiftDate >= :startDate
        ORDER BY s.shiftDate ASC
    """)
    List<Shift> findByShiftRequestAndShiftDateGreaterThanEqual(
                @Param("shiftRequest") ShiftRequest shiftRequest,
                @Param("startDate") LocalDate startDate
    );


    @Query(
        value = """
        SELECT
        s.id            AS  id,
        s.externalId    AS  externalId,
        s.shiftDate     AS  shiftDate,
        s.startTs       AS  startTs,
        s.endTs         AS  endTs,
        s.status        AS  status
        FROM Shift s
        JOIN s.assignment sa
        WHERE sa.externalId = :shiftAssignmentExternalId
        AND s.shiftDate >= CURRENT_DATE
        ORDER BY s.shiftDate ASC, s.startTs ASC
    """
    )
    List<ShiftProjection> findUpcomingByShiftAssignmentExternalId(
                @Param("shiftAssignmentExternalId") UUID shiftAssignmentExternalId,
                Pageable pageable
    );


        @Query(
                value = """
                SELECT
                sh.id               AS id,
                sh.externalId       AS externalId,
                sh.shiftDate        AS shiftDate,
                sh.startTs          AS startTs,
                sh.endTs            AS endTs,
                sh.status           AS status,
                s.name              AS siteName,
                shr.code            AS shiftRequestCode,
                e.name              AS employeeName,
                e.fatherSurname     AS employeeFatherSurname
                FROM Shift sh
                LEFT JOIN sh.site s
                LEFT JOIN s.project p
                LEFT JOIN sh.shiftRequest shr
                LEFT JOIN sh.assignment sa
                LEFT JOIN sa.employee e
                WHERE   (:ignoreProjectFilter = true OR p.id IN :projectIds)
                AND     (:startDate IS NULL = true OR sh.shiftDate >= :startDate)
                AND     (:endExclusiveDate IS NULL = true OR sh.shiftDate < :endExclusiveDate)
                AND     (:siteExternalId IS NULL = true OR s.externalId = :siteExternalId)
                AND     (:projectExternalId IS NULL = true OR p.externalId = :projectExternalId)
                AND     (:shiftStatus IS NULL = true OR sh.status = :shiftStatus)
                """,
                countQuery = """
                SELECT COUNT(sh.id)
                FROM Shift sh
                LEFT JOIN sh.site s
                LEFT JOIN s.project p
                WHERE   (:ignoreProjectFilter = true OR p.id IN :projectIds)
                AND     (:startDate IS NULL OR sh.shiftDate >= :startDate)
                AND     (:endExclusiveDate IS NULL = true OR sh.shiftDate < :endExclusiveDate)
                AND     (:siteExternalId IS NULL = true OR s.externalId = :siteExternalId)
                AND     (:projectExternalId IS NULL = true OR p.externalId = :projectExternalId)
                AND     (:shiftStatus IS NULL = true OR sh.status = :shiftStatus)
                """
        )
        Page<ShiftProjection> findPageByProjectIds(
                @Param("ignoreProjectFilter") boolean ignoreProjectFilter,
                @Param("projectIds") List<Long> projectIds,
                @Param("startDate") LocalDate startDate,
                @Param("endExclusiveDate") LocalDate endExclusiveDate,
                @Param("siteExternalId") UUID siteExternalId,
                @Param("projectExternalId") UUID projectExternalId,
                @Param("shiftStatus") ShiftStatus status,
                Pageable pageable
        );

}
