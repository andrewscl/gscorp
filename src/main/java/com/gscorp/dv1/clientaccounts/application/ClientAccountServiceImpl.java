package com.gscorp.dv1.clientaccounts.application;

import java.util.Collections;
import java.util.List;

import org.springframework.http.HttpStatus;
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
import com.gscorp.dv1.users.application.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClientAccountServiceImpl implements ClientAccountService {

    private final UserService userService;
    private final ClientAccountRepository clientAccountRepository;
    private final ClientService clientService;
    
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
            saved.getClient() != null ? saved.getClient().getName() : null
        );
    }

    // implementación: resuelve userId y delega
    @Override
    @Transactional
    public ClientAccountDto createClientAccountForPrincipal(CreateClientAccountRequest req, Authentication authentication) {
        Long userId = userService.getUserIdFromAuthentication(authentication);
        return createClientAccount(req, userId);
    }


}
