package com.gscorp.dv1.admin.clients.web.dto;

import java.util.UUID;

import com.gscorp.dv1.admin.clients.infrastructure.Client;
import com.gscorp.dv1.admin.companies.web.dto.CompanyDto;
import com.gscorp.dv1.enums.ClientStatus;

public record ClientWithCompanyDto (
    Long id,
    UUID externalId,
    String name,
    String legalName,
    String taxId,
    String email,
    String phone,
    ClientStatus status,
    Boolean active,
    CompanyDto company
){
    public static ClientWithCompanyDto fromEntity(Client client) {
        if (client == null) return null;

        return new ClientWithCompanyDto(
            client.getId(),
            client.getExternalId(),
            client.getName(),
            client.getLegalName(),
            client.getTaxId(),
            client.getContactEmail(),
            client.getContactPhone(),
            client.getStatus(),
            client.getActive(),
            CompanyDto.fromEntity(client.getCompany())
        );
    }
}
