package com.gscorp.dv1.admin.clients.application;

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

import com.gscorp.dv1.admin.clients.infrastructure.Client;
import com.gscorp.dv1.admin.clients.infrastructure.ClientRepository;
import com.gscorp.dv1.admin.clients.infrastructure.projections.ClientProjection;
import com.gscorp.dv1.admin.clients.infrastructure.projections.ClientSelectProjection;
import com.gscorp.dv1.admin.clients.web.dto.ClientDto;
import com.gscorp.dv1.admin.clients.web.dto.ClientSelectDto;
import com.gscorp.dv1.admin.clients.web.dto.CreateClientRequest;
import com.gscorp.dv1.admin.companies.infrastructure.Company;
import com.gscorp.dv1.admin.companies.infrastructure.specification.CompanyRepository;
import com.gscorp.dv1.config.security.SecurityUser;
import com.gscorp.dv1.enums.ClientStatus;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService{

    private final ClientRepository clientRepo;
    private final CompanyRepository companyRepo;

    @Transactional
    public ClientDto createClient (CreateClientRequest request){

        Authentication authentication =
                        SecurityContextHolder.getContext().getAuthentication();

        String currentUser = (authentication != null) ?
                                        authentication.getName() : "SYSTEM";

        Company company = companyRepo.findByExternalId(request.companyExternalId())
                        .orElseThrow(() -> new EntityNotFoundException(
                            "Company not found: " + request.companyExternalId()));

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

    @Transactional(readOnly = true)
    public Client findById (Long id){
        return clientRepo.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Client no encontrado"));
    }

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

    public Client findByIdWithUsers (Long id){
        return clientRepo.findById(id)
            .orElseThrow(()->
                new IllegalArgumentException("Usuario no encontrado" + id));
    }

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


    @Transactional(readOnly = true)
    @Cacheable(value = "userClientIds", key = "#userExternalId")
    public List<Long> getClientIdsByUserExternalId(UUID userExternalId) {
        if(userExternalId == null) return Collections.emptyList();
        List<Long> ids = clientRepo.findClientIdsByUserExternalId(userExternalId);
        return ids == null ? Collections.emptyList() : ids;
    }


    @Transactional(readOnly = true)
    public List<Long> getClientIdsForAuthentication(
            Authentication authentication
    ) {

            Object principal = authentication.getPrincipal();
            SecurityUser securityUser = (SecurityUser) principal;
            UUID externalId = securityUser.getUser().getExternalId();

        return getClientIdsByUserExternalId(externalId);
    }


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


    @Transactional(readOnly = true)
    public List<ClientDto> getClients(SecurityUser securityUser) {
        if ( securityUser.getUser().getRole() == null) {
            throw new IllegalStateException("User role cannot be null");
        }
        boolean isAdmin = securityUser.isAdministrator();
        List<ClientProjection> projections = isAdmin
            ? clientRepo.findAllProjections()
            : clientRepo.findClientsByUserExternalId(
                                securityUser.getUser().getExternalId());
        return projections.stream()
                    .map(ClientDto::fromProjection)
                    .toList();
    }


    @Transactional(readOnly = true)
    public List<ClientSelectDto> getAllClientsSelectDto() {
        List<ClientSelectProjection> rows = clientRepo.findAllSelectProjections();
        return rows
        .stream()
        .map(ClientSelectDto::fromProjection)
        .collect(Collectors.toList());  
    }


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
    public List<ClientDto> getClientsByCompanyAndStatus(
                                    SecurityUser securityUser,
                                    UUID companyExternalId,
                                    ClientStatus status
            ) {
        if ( securityUser.getUser().getRole() == null) {
            throw new IllegalStateException("User role cannot be null");
        }
        boolean isAdmin = securityUser.isAdministrator();
        List<ClientProjection> projections = isAdmin
            ? clientRepo.findClientsByCompanyAndStatus(companyExternalId, status)
            : clientRepo.findClientsByCompanyAndStatusAndUserExternalId(
                companyExternalId, status, securityUser.getUser().getExternalId());
        return projections.stream()
                    .map(ClientDto::fromProjection)
                    .toList();
    }

    @Transactional(readOnly = true)
    public ClientDto getClientByExternalId(UUID externalId) {
        return clientRepo.findProjectionByExternalId(externalId)
                         .map(ClientDto::fromProjection)
                         .orElseThrow(() -> new IllegalArgumentException("Client not found"));
    }


}
