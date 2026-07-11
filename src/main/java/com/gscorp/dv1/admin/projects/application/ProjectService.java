package com.gscorp.dv1.admin.projects.application;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.gscorp.dv1.admin.clients.infrastructure.Client;
import com.gscorp.dv1.admin.projects.infrastructure.Project;
import com.gscorp.dv1.admin.projects.web.dto.ProjectDto;
import com.gscorp.dv1.admin.projects.web.dto.ProjectSelectDto;

public interface ProjectService {
    
    List<Project> findAllWithClientsAndEmployees();
    Optional<Project> findById (Long id);
    Project findByIdWithClients (Long id);
    Client findClientById (Long clientId);
    Project saveProject (Project project);
    void deleteById(Long id);

    //Para exponer a Controller
    List<ProjectDto> findAllById(Set<Long> ids);

    //Para Logica de negocio
    List<Project> findEntitiesById(Set<Long> ids);

    List<ProjectDto> findAll();

    List<ProjectSelectDto> findByClientId(Long clientId);

    List<ProjectDto> findByUserExternalId(UUID userExternalId);

    List<ProjectSelectDto>
            findProjectSelectDtosByEmployeeExternalId(UUID externalId);

}