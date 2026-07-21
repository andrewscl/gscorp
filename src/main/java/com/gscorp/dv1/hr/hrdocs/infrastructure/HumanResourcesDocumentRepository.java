package com.gscorp.dv1.hr.hrdocs.infrastructure;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.gscorp.dv1.hr.hrdocs.infrastructure.projections.HumanResourcesDocumentProjection;

@Repository
public interface HumanResourcesDocumentRepository
                    extends JpaRepository <HumanResourcesDocument, Long> {

    @Query(value = """
        SELECT
        hrd.id                  AS id,
        hrd.externalId          AS externalId,
        hrd.fileUrl             AS fileUrl,
        hrd.createdBy           AS createdBy,
        hrd.updatedBy           AS updatedBy,
        hrd.createdAt           AS createdAt,
        hrd.updatedAt           AS updatedAt,
        e.id                    AS employeeId,
        e.externalId            AS employeeExternalId,
        e.name                  AS employeeName,
        e.fatherSurname         AS employeeFatherSurname,
        e.rut                   AS employeeRut,
        et.id                   AS employeeTerminationId,
        et.externalId           AS employeeTerminationExternalId,
        hdt.id                  AS hrDocumentTypeId,
        hdt.externalId          AS hrDocumentTypeExternalId,
        hdt.name                AS hrDocumentTypeName
        FROM HumanResourcesDocument hrd
        LEFT JOIN hrd.employeeTermination et
        LEFT JOIN hrd.employee e
        LEFT JOIN hrd.hrDocumentType hdt
        WHERE et.externalId = :employeeTerminationExternalId
        """)
    List<HumanResourcesDocumentProjection> findByEmployeeTerminationExternalId(
                                @Param("employeeTerminationExternalId") UUID externalId);

    
}
