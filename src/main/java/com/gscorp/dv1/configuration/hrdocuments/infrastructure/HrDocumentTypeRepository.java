package com.gscorp.dv1.configuration.hrdocuments.infrastructure;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.gscorp.dv1.configuration.hrdocuments.infrastructure.projections.HrDocumentTypeProjection;
import com.gscorp.dv1.enums.EmployeeStatus;
import com.gscorp.dv1.enums.HrProcessType;

@Repository
public interface HrDocumentTypeRepository 
                    extends JpaRepository <HrDocumentType, Long> {

    @Query(
        value = """
        SELECT
        hdt.id                  AS id,
        hdt.externalId          AS externalId,
        hdt.name                AS name,
        hdt.required            AS required,
        hdt.status              AS status,
        hdt.targetProcess       AS targetProcess,
        hdt.createdBy           AS createdBy,
        hdt.updatedBy           AS updatedBy,
        hdt.createdAt           AS createdAt,
        hdt.updatedAt           AS updatedAt
        FROM HrDocumentType hdt
        WHERE (:status IS NULL OR hdt.status = :status)
            AND(:process IS NULL OR hdt.targetProcess = :process)
        ORDER BY hdt.name ASC
        """,
        countQuery = """
        SELECT COUNT(hdt.id)
        FROM HrDocumentType hdt
        WHERE (:status IS NULL OR hdt.status = :status)
            AND(:process IS NULL OR hdt.targetProcess = :process)

        """
    )
    Page<HrDocumentTypeProjection> findByStatusAndProcess(
        @Param("status") EmployeeStatus status,
        @Param("process") HrProcessType process,
        Pageable pageable
    );

}
