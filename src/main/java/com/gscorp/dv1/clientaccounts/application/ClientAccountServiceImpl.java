package com.gscorp.dv1.clientaccounts.application;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.gscorp.dv1.clientaccounts.infrastructure.ClientAccount;
import com.gscorp.dv1.clientaccounts.infrastructure.ClientAccountRepository;
import com.gscorp.dv1.clientaccounts.web.dto.ClientAccountDto;
import com.gscorp.dv1.clientaccounts.web.dto.CreateClientAccountRequest;
import com.gscorp.dv1.clients.application.ClientService;
import com.gscorp.dv1.clients.infrastructure.Client;
import com.gscorp.dv1.clients.infrastructure.ClientMembershipRepository;
import com.gscorp.dv1.sites.application.SiteService;
import com.gscorp.dv1.users.application.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClientAccountServiceImpl implements ClientAccountService {

    private final UserService userService;
    private final ClientAccountRepository clientAccountRepository;
    private final ClientService clientService;
    private final SiteService siteService;
    private final ClientMembershipRepository clientMembershipRepository;
    
    /**
     * Devuelve una lista de ClientAccountDto asociadas a todos los Clients del usuario.
     *
     * @param userId id del usuario
     * @return lista (posiblemente vacía) de ClientAccountDto
     */
    @Override
    @Transactional(readOnly = true)
    public List<ClientAccountDto> findAccountDtosForUser(Long userId) {
        if (userId == null) return Collections.emptyList();

        List<Long> clientIds = userService.getClientIdsForUser(userId);
        if (clientIds == null || clientIds.isEmpty()) {
            return Collections.emptyList();
        }

        // El repositorio ya tiene la consulta que devuelve ClientAccountDto vía JPQL constructor expression
        return clientAccountRepository.findDtoByClientIds(clientIds);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClientAccountDto> findAccountDtosForPrincipal(Authentication authentication) {
        Long userId = userService.getUserIdFromAuthentication(authentication);
        return findAccountDtosForUser(userId);
    }

    @Override
    @Transactional
    public ClientAccountDto createClientAccount(CreateClientAccountRequest req, Long userId) {
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado");
        }

        List<Long> allowedClientIds = userService.getClientIdsForUser(userId);
        if (allowedClientIds == null || !allowedClientIds.contains(req.clientId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No autorizado para usar ese cliente");
        }

        // clientService.findById(...) aquí devuelve Client o null (dependiendo de tu API)
        Client client = clientService.findById(req.clientId());
        if (client == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cliente no encontrado");
        }

        // Normaliza / valida nombre u otras reglas de negocio aquí
        String name = req.name().trim();

        // Construye entidad (ajusta según tu builder/constructor real)
        ClientAccount entity = ClientAccount.builder()
            .name(name)
            .client(client)
            .notes(req.notes())
            .build();

        ClientAccount saved = clientAccountRepository.save(entity);

        return new ClientAccountDto(
            saved.getId(),
            saved.getName(),
            saved.getClient() != null ? saved.getClient().getId() : null,
            saved.getClient() != null ? saved.getClient().getName() : null,
            saved.getNotes()
        );
    }

    // implementación: resuelve userId y delega
    @Override
    @Transactional
    public ClientAccountDto createClientAccountForPrincipal(CreateClientAccountRequest req, Authentication authentication) {
        Long userId = userService.getUserIdFromAuthentication(authentication);
        return createClientAccount(req, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public ClientAccountDto getAccountDtoIfOwned(Long accountId, Long userId) {
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado");
        }

        List<Long> allowedClientIds = userService.getClientIdsForUser(userId);
        if (allowedClientIds == null || allowedClientIds.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No autorizado para ver esta cuenta");
        }

        ClientAccount account = clientAccountRepository.findById(accountId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cuenta no encontrada"));

        if (account.getClient() == null || !allowedClientIds.contains(account.getClient().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No autorizado para ver esta cuenta");
        }

        return new ClientAccountDto(
            account.getId(),
            account.getName(),
            account.getClient() != null ? account.getClient().getId() : null,
            account.getClient() != null ? account.getClient().getName() : null,
            account.getNotes()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClientAccountDto> getClientAccountsForSite(Long siteId, Long userId) {
        if( siteId == null) return List.of();

        // resolver clientId via SiteService (devuelve Optional)
        Optional<Long> clientIdOpt = siteService.getClientIdForSite(siteId);
        if (clientIdOpt.isEmpty()) {
            return List.of();
        }
        Long clientId = clientIdOpt.get();

        // validar userId (servicio recibe userId desde controller / security)
        if (userId == null) {
            throw new AuthenticationCredentialsNotFoundException("Usuario no autenticado");
        }

        // validar membership: si no es miembro -> AccessDenied (403)
        boolean member = clientMembershipRepository.existsByClientIdAndUserId(clientId, userId);
        if (!member) {
            throw new AccessDeniedException("Usuario no pertenece al cliente asociado al site");
        }

        // devolver accounts del client (tu repo ya provee findDtoByClientIds)
        return clientAccountRepository.findDtoByClientIds(List.of(clientId));
    
    }

}
