package com.gscorp.dv1.clients.web.dto;

import com.gscorp.dv1.clients.infrastructure.Client;
import com.gscorp.dv1.clients.infrastructure.ClientSelectProjection;

public record ClientSelectDto (
    Long id,
    String name
) {
    public static ClientSelectDto fromEntity(Client client) {
        if (client == null) return null;
        return new ClientSelectDto(client.getId(), client.getName());
    }

    public static ClientSelectDto fromProjection(ClientSelectProjection p) {
        if (p == null) return null;
        return new ClientSelectDto(p.getId(), p.getName());
    }
}