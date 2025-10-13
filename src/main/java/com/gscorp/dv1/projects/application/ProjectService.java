package com.gscorp.dv1.projects.application;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.gscorp.dv1.clients.infrastructure.Client;
import com.gscorp.dv1.projects.infrastructure.Project;

@Service
public interface ProjectService {
    
    List<Project> findAllWithClients();
    Optional<Project> findById (Long id);
    Project findByIdWithClients (Long id);
    Client findClientById (Long clientId);
    Project saveProject (Project project);
    void deleteById(Long id);
}