package com.gscorp.dv1.projects.infrastructure.application;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gscorp.dv1.projects.infrastructure.Project;
import com.gscorp.dv1.projects.infrastructure.ProjectRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService{
    
    private final ProjectRepository projectRepository;

    @Override
    @Transactional
    public List<Project> findAll (){
        return projectRepository.findAll();
    }

    @Override
    @Transactional
    public Optional<Project> findById(Long id) {
        return projectRepository.findById(id);
    }

    @Override
    public Project findByIdWithClients (Long id){
        return projectRepository.findById(id)
            .orElseThrow(()->
                new IllegalArgumentException("Usuario no encontrado" + id));
    }

}
