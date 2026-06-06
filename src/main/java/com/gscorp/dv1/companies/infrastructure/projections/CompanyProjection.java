package com.gscorp.dv1.companies.infrastructure.projections;

import java.util.UUID;

import com.gscorp.dv1.enums.CompanyStatus;

public interface CompanyProjection {
    Long getId();
    UUID getExternalId();
    String getName();
    String getLegalName();
    String getTaxId();
    CompanyStatus getStatus();
}
