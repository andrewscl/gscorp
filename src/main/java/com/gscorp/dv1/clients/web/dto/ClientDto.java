package com.gscorp.dv1.clients.web.dto;

import java.util.UUID;

import com.gscorp.dv1.clients.infrastructure.Client;
import com.gscorp.dv1.enums.ClientStatus;

public record ClientDto (

    Long id,
    UUID externalId,
    String name,
    String legalName,
    String taxId,
    String contactEmail,
    ClientStatus status,
    Boolean active)
    {

        public static ClientDto fromEntity (Client client) {
            if (client == null) return null;

            return new ClientDto(
                client.getId(),
                client.getExternalId(),
                client.getName(),
                client.getLegalName(),
                client.getTaxId(),
                client.getContactEmail(),
                client.getStatus(),
                client.getActive()
            );

        }

}
