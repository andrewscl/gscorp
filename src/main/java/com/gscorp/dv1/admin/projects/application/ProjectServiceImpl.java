package com.gscorp.dv1.admin.projects.application;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.gscorp.dv1.admin.clients.application.ClientService;
import com.gscorp.dv1.admin.clients.infrastructure.Client;
import com.gscorp.dv1.admin.clients.infrastructure.ClientRepository;
import com.gscorp.dv1.admin.projects.infrastructure.Project;
import com.gscorp.dv1.admin.projects.infrastructure.ProjectRepository;
import com.gscorp.dv1.admin.projects.infrastructure.projections.ProjectProjection;
import com.gscorp.dv1.admin.projects.web.dto.ProjectDto;
import com.gscorp.dv1.admin.projects.web.dto.ProjectSelectDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService{
    
    private final ProjectRepository projectRepository;
    private final ClientRepository clientRepository;
    private final ClientService clientService;

    @Transactional(readOnly = true)
    public List<Project> findAllWithClientsAndEmployees (){
        return projectRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Project> findById(Long id) {
        return projectRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Project findByIdWithClients (Long id){
        return projectRepository.findById(id)
            .orElseThrow(()->
                new IllegalArgumentException("Usuario no encontrado" + id));
    }

    @Transactional(readOnly = true)
    public Client findClientById(Long clientId) {
        return clientRepository.findById(clientId)
                .orElseThrow(() ->
                    new IllegalArgumentException("Cliente no encontrado: " + clientId));
    }

    @Transactional
    public Project saveProject(Project project) {
        return projectRepository.save(project);
    }

    @Transactional
    public void deleteById(Long id) {
        if (!projectRepository.existsById(id)) {
            throw new IllegalArgumentException("Proyecto no encontrado");
        }
        try {
            projectRepository.deleteById(id);
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalStateException("No se puede eliminar: el proyecto tiene referencias");
        }
    }

    @Transactional (readOnly = true)
    public List<ProjectDto> findAllById(Set<Long> ids) {
        List<Project> projects = projectRepository.findAllByIdWithEmployees(ids);
        return projects.stream()
                .map(ProjectDto::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Project> findEntitiesById(Set<Long> ids) {
        return projectRepository.findAllByIdWithEmployees(ids);
    }

    @Transactional(readOnly = true)
    public List<ProjectDto> findAll() {
        return projectRepository.findAll().stream()
                .map(ProjectDto::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProjectSelectDto> findByClientId(Long clientId) {
        if (clientId == null) return List.of();
        return projectRepository.findDtoByClientId(clientId);
    }

    @Transactional(readOnly = true)
    public List<ProjectDto> findByUserExternalId (UUID userExternalId) {
        if (userExternalId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado");
        }
        List<Long> clientIds = clientService.getClientIdsByUserExternalId(userExternalId);
        if (clientIds == null || clientIds.isEmpty()) {
            log.debug("No clientIds for user {} -> returning zero series for {}..{}", userExternalId);
            return List.of();
        }
        List<ProjectProjection> projections = projectRepository.findByClientIds(clientIds);
        if (projections == null || projections.isEmpty()) {
                return List.of();
        }
        return projections.stream()
                .map(ProjectDto::fromProjection)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProjectSelectDto>
                    findProjectSelectDtosByEmployeeExternalId(UUID employeeExternalId){
        return projectRepository
                .findProjectSelectProjectionsByEmployeeExternalId(employeeExternalId)
                .stream()
                .map(ProjectSelectDto::fromSelectProjection)
                .toList();
    }




}
