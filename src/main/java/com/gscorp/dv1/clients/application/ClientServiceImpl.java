package com.gscorp.dv1.clients.application;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gscorp.dv1.clients.infrastructure.Client;
import com.gscorp.dv1.clients.infrastructure.ClientSelectProjection;
import com.gscorp.dv1.clients.infrastructure.ClientRepository;
import com.gscorp.dv1.clients.web.dto.ClientSelectDto;
import com.gscorp.dv1.clients.web.dto.ClientWithCompanyDto;
import com.gscorp.dv1.clients.web.dto.CreateClientRequest;
import com.gscorp.dv1.companies.infrastructure.Company;
import com.gscorp.dv1.companies.infrastructure.CompanyRepository;
import com.gscorp.dv1.enums.ClientStatus;
import com.gscorp.dv1.security.SecurityUser;
import com.gscorp.dv1.clients.web.dto.ClientDto;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService{

    private final ClientRepository clientRepo;
    private final CompanyRepository companyRepo;

    @Override
    @Transactional
    public ClientDto createClient (CreateClientRequest request){

        Authentication authentication =
                        SecurityContextHolder.getContext().getAuthentication();

        String currentUser = (authentication != null) ?
                                        authentication.getName() : "SYSTEM";

        Company company = companyRepo.getReferenceById(request.companyId());

        Client client = Client.builder()
                            .name(request.name())
                            .legalName(request.legalName())
                            .taxId(request.taxId())
                            .contactEmail(request.email())
                            .contactPhone(request.phone())
                            .status(ClientStatus.ACTIVE)
                            .createdBy(currentUser)
                            .company(company)
                            .build();

        Client savedClient = clientRepo.save(client);

        return ClientDto.fromEntity(savedClient);
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
                    .map(c -> new ClientDto(
                                    c.getId(),
                                    c.getExternalId(),
                                    c.getName(),
                                    c.getLegalName(),
                                    c.getTaxId(),
                                    c.getContactEmail(),
                                    c.getStatus(),
                                    c.getActive()))
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
    public List<Long> getClientIdsByUserExternalId(UUID userExternalId) {
        if(userExternalId == null) return Collections.emptyList();
        List<Long> ids = clientRepo.findClientIdsByUserExternalId(userExternalId);
        return ids == null ? Collections.emptyList() : ids;
    }


    @Override
    @Transactional(readOnly = true)
    public List<Long> getClientIdsForAuthentication(
            Authentication authentication
    ) {

            Object principal = authentication.getPrincipal();
            SecurityUser securityUser = (SecurityUser) principal;
            UUID externalId = securityUser.getUser().getExternalId();

        return getClientIdsByUserExternalId(externalId);
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
    @Transactional
    public void ensureUserHasAccess(Authentication authentication, Collection<Long> clientIds) {
        if (clientIds == null || clientIds.isEmpty()) return;

        Object principal = authentication.getPrincipal();
        SecurityUser securityUser = (SecurityUser) principal;
        UUID externalId = securityUser.getUser().getExternalId();        

        List<Long> allowed = getClientIdsByUserExternalId(externalId);
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
                .map(c -> new ClientDto(
                                c.id(),
                                c.externalId(),
                                c.name(),
                                c.legalName(),
                                c.taxId(),
                                c.contactEmail(),
                                c.status(),
                                c.active())
                )

                .collect(Collectors.toList());
    }


    @Override
    public List<ClientSelectDto> findClientsByUserExternalId(UUID userExternalId) {

        List<ClientSelectProjection> rows = clientRepo.findClientsByUserExternalId(userExternalId);

        return rows
        .stream()
        .map(ClientSelectDto::fromProjection)
        .collect(Collectors.toList());  
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClientSelectDto> getAllClientsSelectDto() {

        List<ClientSelectProjection> rows = clientRepo.findAllProjections();

        return rows
        .stream()
        .map(ClientSelectDto::fromProjection)
        .collect(Collectors.toList());  
    }

    @Override
    @Transactional(readOnly = true)
    public List<Client> validateAndFindAllById(Set<Long> ids) {
        if(ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }

        List<Client> clients = clientRepo.findAllById(ids);

        if (clients.size() != ids.size()) {
            throw new IllegalArgumentException("Some client IDs are invalid");
        }

        return clients;
    }


    @Transactional(readOnly = true)
    public List<ClientWithCompanyDto> getAllClientsWithCompany() {
        return clientRepo.findAll().stream()
            .map(ClientWithCompanyDto::fromEntity)
            .toList();
    }

    @Transactional(readOnly = true)
    public ClientWithCompanyDto getClientWithCompanyById(Long id) {
        return clientRepo.findById(id)
            .map(ClientWithCompanyDto::fromEntity)
            .orElseThrow(() -> new EntityNotFoundException("Client no encontrado: " + id));
    }

    @Transactional(readOnly = true)
    public ClientWithCompanyDto getClientWithCompanyByExternalId(UUID externalId) {

        Client client = clientRepo.findByExternalId(externalId);
        if(client == null){
            throw new EntityNotFoundException("Client not found" + externalId);
        }
        
    return ClientWithCompanyDto.fromEntity(client);
    }

}
