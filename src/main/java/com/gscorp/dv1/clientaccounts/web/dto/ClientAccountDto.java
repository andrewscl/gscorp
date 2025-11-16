package com.gscorp.dv1.clientaccounts.web.dto;

import com.gscorp.dv1.clientaccounts.infrastructure.ClientAccount;

public record ClientAccountDto (
    Long id,
    String name,
    Long clientId,
    String clientName,
    String notes
){

    public static ClientAccountDto fromEntity(ClientAccount ca) {
        if (ca == null) return null;
        return new ClientAccountDto(
            ca.getId(),
            ca.getName(),
            ca.getClient() != null ? ca.getClient().getId() : null,
            ca.getClient() != null ? ca.getClient().getName() : null,
            ca.getNotes()
        );
    }

}
