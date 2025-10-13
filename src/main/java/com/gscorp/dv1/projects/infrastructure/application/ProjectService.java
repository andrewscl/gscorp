package com.gscorp.dv1.projects.infrastructure.application;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.gscorp.dv1.projects.infrastructure.Project;

@Service
public interface ProjectService {
    
    List<Project> findAll();
    Optional<Project> findById (Long id);
    Project findByIdWithClients (Long id);
}