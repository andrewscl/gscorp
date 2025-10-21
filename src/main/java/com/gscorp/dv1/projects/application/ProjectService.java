package com.gscorp.dv1.projects.application;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.gscorp.dv1.clients.infrastructure.Client;
import com.gscorp.dv1.projects.infrastructure.Project;

public interface ProjectService {
    
    List<Project> findAllWithClientsAndEmployees();
    Optional<Project> findById (Long id);
    Project findByIdWithClients (Long id);
    Client findClientById (Long clientId);
    Project saveProject (Project project);
    void deleteById(Long id);
    List<Project> findAllById(Set<Long> ids);
    List<Project> findAll();
}