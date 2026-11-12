package com.gscorp.dv1.admin.clients.application;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.security.core.Authentication;

import com.gscorp.dv1.admin.clients.infrastructure.Client;
import com.gscorp.dv1.admin.clients.web.dto.ClientDto;
import com.gscorp.dv1.admin.clients.web.dto.ClientSelectDto;
import com.gscorp.dv1.admin.clients.web.dto.CreateClientRequest;
import com.gscorp.dv1.config.security.SecurityUser;
import com.gscorp.dv1.enums.ClientStatus;

public interface ClientService {

    ClientDto createClient (CreateClientRequest request);

    Client findById (Long id);
    List<ClientDto> getAllClients();
    Client findByIdWithUsers (Long id);
    void deleteById(Long id);

    List<Long> getClientIdsByUserExternalId(UUID userExternalId);
    
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

    List<ClientDto> getClients(SecurityUser securityUser);

    List<ClientSelectDto> getAllClientsSelectDto();

    List<Client> validateAndFindAllById(Set<Long> ids);

    List<ClientDto> getClientsByCompanyAndStatus(
                            SecurityUser securityUser,
                            UUID companyExternalId,
                            ClientStatus status
                        );

    ClientDto getClientByExternalId(UUID externalId);

}
