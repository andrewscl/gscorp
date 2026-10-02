package com.gscorp.dv1.admin.clientaccounts.application;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gscorp.dv1.admin.clientaccounts.infrastructure.ClientAccount;
import com.gscorp.dv1.admin.clientaccounts.infrastructure.ClientAccountRepository;
import com.gscorp.dv1.admin.clientaccounts.web.dto.ClientAccountDto;
import com.gscorp.dv1.admin.clientaccounts.web.dto.CreateClientAccountRequest;

import jakarta.persistence.EntityNotFoundException;

import com.gscorp.dv1.admin.projects.application.ProjectService;
import com.gscorp.dv1.admin.projects.infrastructure.Project;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClientAccountServiceImpl implements ClientAccountService {

    private final ClientAccountRepository clientAccountRepository;
    private final ProjectService projectService;


    @Override
    @Transactional(readOnly = true)
    public List<ClientAccountDto> getDtosByUserScope(
                                boolean ignoreProjectFilter,
                                List<Long> projectIds) {
        List<ClientAccount> accounts = clientAccountRepository.findClientAccountsByUser(
                                                            ignoreProjectFilter, projectIds);
        return accounts.stream()
                        .map(ClientAccountDto::fromEntity)
                        .toList();
    }

    @Override
    @Transactional
    public ClientAccountDto createClientAccount(
                                boolean ignoreProjectFilter,
                                List<Long> projectIds,
                                CreateClientAccountRequest req,
                                UUID projectExternalId) {
        Project project = projectService.findByExternalId(ignoreProjectFilter, projectIds, projectExternalId)
            .orElseThrow(() -> new EntityNotFoundException("El proyecto no existe o no existe acceso"));
        String name = req.name().trim();
        ClientAccount entity = ClientAccount.builder()
            .name(name)
            .project(project)
            .notes(req.notes())
            .build();
        ClientAccount saved = clientAccountRepository.save(entity);
        return ClientAccountDto.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ClientAccountDto getAccountDtoByExternalId(
                                    boolean ignoreProjectFilter,
                                    List<Long> projectIds,
                                    UUID externalId) {
        if (externalId == null) {
            throw new EntityNotFoundException("Cuenta no encontrada o sin permisos de acceso.");
        }
        ClientAccount account = clientAccountRepository.findByExternalId(ignoreProjectFilter, projectIds, externalId)
            .orElseThrow(() -> new EntityNotFoundException("Cuenta no encontrada"));
        return ClientAccountDto.fromEntity(account);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClientAccountDto> getClientAccountsBySite(
                                boolean ignoreProjectFilter,
                                List<Long> projectIds,
                                UUID siteExternalId
                            ) {
        if( siteExternalId == null) return List.of();
        List<ClientAccount> accounts = clientAccountRepository.findClientAccountsBySite(
                                ignoreProjectFilter, 
                                projectIds,
                                siteExternalId);
        return accounts.stream()
                        .map(ClientAccountDto::fromEntity)
                        .toList();
    }

}
