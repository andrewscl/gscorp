package com.gscorp.dv1.clients.web.dto;

import com.gscorp.dv1.clients.infrastructure.Client;
import com.gscorp.dv1.clients.infrastructure.ClientBriefProjection;

public record ClientBriefDto (
    Long id,
    String name,
    Boolean active
) {
    public static ClientBriefDto fromEntity(Client client) {
        if (client == null) return null;
        return new ClientBriefDto(client.getId(), client.getName(), client.getActive());
    }

    public static ClientBriefDto fromProjection(ClientBriefProjection p) {
        if (p == null) return null;
        return new ClientBriefDto(p.getId(), p.getName(), p.getActive());
    }
}