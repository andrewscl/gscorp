package com.gscorp.dv1.operations.shiftassignments.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.gscorp.dv1.enums.ShiftAssignmentStatus;
import com.gscorp.dv1.operations.shiftassignments.infrastructure.projections.ShiftAssignmentProjection;

@Repository
public interface ShiftAssignmentRepository extends JpaRepository<ShiftAssignment, Long> {

    @Query(
        value = """
        SELECT
        sa.id                       AS id,
        sa.externalId               AS externalId,
        s.name                      AS siteName,
        sr.externalId               AS shiftRequestExternalId,
        sr.Id                       AS shiftRequestId,
        sr.code                     AS shiftRequestCode,
        e.externalId                AS employeeExternalId,
        e.name                      AS employeeName,
        e.fatherSurname             AS employeeFatherSurname,
        e.rut                       AS employeeRut,
        sa.status                   AS status,
        sa.notes                    AS notes,
        sa.assignedAt               AS assignedAt,
        sa.assignedUntil            AS assignedUntil,
        sa.startCycleNumber         AS startCycleNumber,
        sa.createdBy                AS createdBy,
        sa.updatedBy                AS updatedBy,
        sa.createdAt                AS createdAt,
        sa.updatedAt                AS updatedAt
        FROM ShiftAssignment sa
        LEFT JOIN sa.shiftRequest sr
        LEFT JOIN sr.site s
        LEFT JOIN sa.employee e
        LEFT JOIN e.projects p
        WHERE p.client.id IN :clientIds
        AND(:status IS NULL OR sa.status = :status)
        """,
        countQuery = """
        SELECT COUNT(sa.id)
        FROM ShiftAssignment sa
        LEFT JOIN sa.employee e
        LEFT JOIN e.projects p
        WHERE p.client.id IN :clientIds
        AND(:status IS NULL OR sa.status = :status)
        """
    )
    Page<ShiftAssignmentProjection> findByClientIds(
        @Param("clientIds") List<Long> clientIds,
        @Param("status") ShiftAssignmentStatus status,
        Pageable pageable
    );

    @EntityGraph(attributePaths = {
        "shiftRequest.site.project", "shiftRequest.shiftPattern", "employee"})
    Optional<ShiftAssignment> findByExternalId(UUID externalId);
}
