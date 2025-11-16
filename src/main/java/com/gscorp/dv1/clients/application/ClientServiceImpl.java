package com.gscorp.dv1.clients.application;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gscorp.dv1.clients.infrastructure.Client;
import com.gscorp.dv1.clients.infrastructure.ClientRepository;
import com.gscorp.dv1.clients.web.dto.ClientDto;
import com.gscorp.dv1.users.application.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService{

    private final ClientRepository clientRepo;
    private final UserService userService;

    @Override
    @Transactional
    public Client saveClient (Client client){
        return clientRepo.save(client);
    }

    @Override
    @Transactional(readOnly = true)
    public Client findById (Long id){
        return clientRepo.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Client no encontrado"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClientDto> getAllClients (){
        return clientRepo.findAll(Sort.by("name").ascending())
                    .stream()
                    .map(c -> new ClientDto(c.getId(), c.getName(), c.getLegalName(), c.getTaxId(), c.getContactEmail(), c.getActive()))
                    .toList();
    }

    @Override
    public Client findByIdWithUsers (Long id){
        return clientRepo.findById(id)
            .orElseThrow(()->
                new IllegalArgumentException("Usuario no encontrado" + id));
    }

    //Eliminar cliente
    @Override
    @Transactional
    public void deleteById(Long id) {
        if (!clientRepo.existsById(id)) {
            throw new IllegalArgumentException("Cliente no encontrado");
        }
        try {
            clientRepo.deleteById(id);
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalStateException("No se puede eliminar: el cliente tiene referencias");
        }
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "userClientIds", key = "#userId")
    public List<Long> getClientIdsByUserId(Long userId) {
        if(userId == null) return Collections.emptyList();
        List<Long> ids = clientRepo.findClientIdsByUserId(userId);
        return ids == null ? Collections.emptyList() : ids;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> getClientIdsForAuthentication(Authentication authentication) {
        Long userId = userService.getUserIdFromAuthentication(authentication);
        return getClientIdsByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> resolveClientIdsOrDefault(Authentication authentication, List<Long> requestedClientIds, Long legacyClientId) {
        // legacy single id -> lista
        if ((requestedClientIds == null || requestedClientIds.isEmpty()) && legacyClientId != null) {
            requestedClientIds = List.of(legacyClientId);
        }
        // si no se pidió nada, devolver los clients del usuario
        if (requestedClientIds == null || requestedClientIds.isEmpty()) {
            return getClientIdsForAuthentication(authentication);
        }
        // validar acceso (admins bypass)
        ensureUserHasAccess(authentication, requestedClientIds);
        return requestedClientIds;
    }

    @Override
    public void ensureUserHasAccess(Authentication authentication, Collection<Long> clientIds) {
        if (clientIds == null || clientIds.isEmpty()) return;

        // Admins pueden ver todo
        if (userService.isAdmin(authentication)) return;

        Long userId = userService.getUserIdFromAuthentication(authentication);
        List<Long> allowed = getClientIdsByUserId(userId);
        if (!allowed.containsAll(clientIds)) {
            throw new AccessDeniedException("Access denied to one or more clients");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClientDto> findDtosByUserId(Long userId) {
        if(userId == null) return Collections.emptyList();

        List<ClientDto> clients = clientRepo.findDtosByUserId(userId);
        if (clients == null || clients.isEmpty()) return Collections.emptyList();

        return clients.stream()
                .map(c -> new ClientDto(c.id(), c.name(), c.legalName(), c.taxId(), c.contactEmail(), c.active()))
                .collect(Collectors.toList());
    }

}
