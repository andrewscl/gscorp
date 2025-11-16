package com.gscorp.dv1.clients.web.dto;

import com.gscorp.dv1.clients.infrastructure.Client;

//DTO Salida
public record ClientDto (

    Long id,
    String name,
    String legalName,
    String taxId,
    String contactEmail,
    Boolean active)
    {

        public static ClientDto fromEntity (Client client) {
            if (client == null) return null;

            return new ClientDto(
                client.getId(),
                client.getName(),
                client.getLegalName(),
                client.getTaxId(),
                client.getContactEmail(),
                client.getActive()
            );

        }

}
