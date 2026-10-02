package com.gscorp.dv1.admin.clientaccounts.web.dto;

import java.util.UUID;

import com.gscorp.dv1.admin.clientaccounts.infrastructure.ClientAccount;

public record ClientAccountDto (
    Long id,
    UUID externalId,
    String name,
    String projectName,
    String notes
){

    public static ClientAccountDto fromEntity(ClientAccount ca) {
        if (ca == null) return null;
        return new ClientAccountDto(
            ca.getId(),
            ca.getExternalId(),
            ca.getName(),
            ca.getProject() != null ? ca.getProject().getName() : null,
            ca.getNotes()
        );
    }

}
