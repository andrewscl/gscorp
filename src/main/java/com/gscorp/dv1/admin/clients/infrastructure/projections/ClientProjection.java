package com.gscorp.dv1.admin.clients.infrastructure.projections;

import java.util.UUID;

import com.gscorp.dv1.enums.ClientStatus;

public interface ClientProjection {

    Long getId();
    UUID getExternalId();
    String getName();
    String getLegalName();
    String getTaxId();
    String getContactEmail();
    ClientStatus getStatus();
    Boolean getActive();
    String getCompanyName();
}
