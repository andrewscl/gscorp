package com.gscorp.dv1.projects.application;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gscorp.dv1.clients.infrastructure.Client;
import com.gscorp.dv1.clients.infrastructure.ClientRepository;
import com.gscorp.dv1.projects.infrastructure.Project;
import com.gscorp.dv1.projects.infrastructure.ProjectRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService{
    
    private final ProjectRepository projectRepository;
    private final ClientRepository clientRepository;

    @Override
    @Transactional
    public List<Project> findAllWithClientsAndEmployees (){
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

    @Override
    public Client findClientById(Long clientId) {
        return clientRepository.findById(clientId)
                .orElseThrow(() ->
                    new IllegalArgumentException("Cliente no encontrado: " + clientId));
    }

    @Override
    @Transactional
    public Project saveProject(Project project) {
        return projectRepository.save(project);
    }

    //Eliminar proyecto
    @Override
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

    @Override
    public List<Project> findAllById(Set<Long> ids) {
        return projectRepository.findAllById(ids);
    }

    @Override
    @Transactional
    public List<Project> findAll() {
        return projectRepository.findAll();
    }

}
