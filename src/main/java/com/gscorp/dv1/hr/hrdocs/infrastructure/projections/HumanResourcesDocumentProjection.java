package com.gscorp.dv1.hr.hrdocs.infrastructure.projections;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface HumanResourcesDocumentProjection {
    Long getId();
    UUID getExternalId();
    String getFileUrl();
    String getCreatedBy();
    String getUpdatedBy();
    OffsetDateTime getCreatedAt();
    OffsetDateTime getUpdatedAt();

    Long getEmployeeId();
    UUID getEmployeeExternalId();
    String getEmployeeName();
    String getEmployeeFatherSurname();
    String getEmployeeRut();

    Long getEmployeeTerminationId();
    UUID getEmployeeTerminationExternalId();

    Long getHrDocumentTypeId();
    UUID getHrDocumentTypeExternalId();
    String getHrDocumentTypeName();

}
