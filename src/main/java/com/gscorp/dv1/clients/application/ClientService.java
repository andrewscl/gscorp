package com.gscorp.dv1.clients.application;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.Authentication;

import com.gscorp.dv1.clients.infrastructure.Client;
import com.gscorp.dv1.clients.web.dto.ClientDto;

public interface ClientService {

    Client saveClient (Client client);
    Client findById (Long id);
    List<ClientDto> getAllClients();
    Client findByIdWithUsers (Long id);
    void deleteById(Long id);
    List<Long> getClientIdsByUserId(Long userId);
    List<Long> getClientIdsForAuthentication(Authentication authentication);
        /**
     * Resuelve una lista final de clientIds teniendo en cuenta:
     * - clientIds solicitados (pueden venir null/empty)
     * - legacy clientId
     * - si no se pasa nada, devuelve los clientIds del usuario autenticado
     */
    List<Long> resolveClientIdsOrDefault(Authentication authentication, List<Long> requestedClientIds, Long legacyClientId);

    /**
     * Valida que authentication tenga acceso a todos los clientIds; lanza AccessDeniedException o ResponseStatusException si no.
     */
    void ensureUserHasAccess(Authentication authentication, Collection<Long> clientIds);

    List<ClientDto> findDtosByUserId(Long userId);
    

}
