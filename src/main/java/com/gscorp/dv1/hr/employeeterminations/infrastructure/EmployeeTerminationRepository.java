package com.gscorp.dv1.hr.employeeterminations.infrastructure;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.gscorp.dv1.enums.EmployeeTransitionStatus;
import com.gscorp.dv1.hr.employeeterminations.infrastructure.projections.EmployeeTerminationProjection;

@Repository
public interface EmployeeTerminationRepository
                    extends JpaRepository <EmployeeTermination, Long> {

    @Query(
        value = """
        SELECT
        etr.id                  AS id,
        etr.externalId          AS externalId,
        etr.requestType         AS requestType,
        etr.terminationReason   AS terminationReason,
        etr.status              AS status,
        etr.proposedExitDate    AS proposedExitDate,
        etr.description         AS description,
        etr.resolvedBy          AS resolvedBy,
        etr.resolvedAt          AS resolvedAt,
        etr.createdBy           AS createdBy,
        etr.updatedBy           AS updatedBy,
        etr.createdAt           AS createdAt,
        etr.updatedAt           AS updatedAt,
        e.id                    AS employeeId,
        e.name                  AS employeeName,
        e.fatherSurname         AS fatherSurname,
        e.rut                   AS rut
        FROM EmployeeTermination etr
        LEFT JOIN etr.employee e
        LEFT JOIN e.projects p
        WHERE p.client.id IN :clientIds
            AND(:status IS NULL OR etr.status = :status)
        ORDER BY etr.createdAt DESC
        """,
        countQuery = """
        SELECT COUNT(etr.id)
        FROM EmployeeTermination etr
        LEFT JOIN etr.employee e
        LEFT JOIN e.projects p
        WHERE p.client.id IN :clientIds
            AND(:status IS NULL OR etr.status = :status)
        """
    )
    Page<EmployeeTerminationProjection> findByClientIds(
        @Param("clientIds") List<Long> clientIds,
        @Param("status") EmployeeTransitionStatus status,
        Pageable pageable
    );

}
