package com.gscorp.dv1.projects.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.gscorp.dv1.clients.infrastructure.Client;
import com.gscorp.dv1.projects.application.ProjectService;
import com.gscorp.dv1.projects.infrastructure.Project;
import com.gscorp.dv1.projects.web.dto.CreateProjectRequest;
import com.gscorp.dv1.projects.web.dto.ProjectDto;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectRestController {
    
    private final ProjectService projectService;

    @PostMapping("/create")
    public ResponseEntity<ProjectDto> createProject(
        @jakarta.validation.Valid @RequestBody CreateProjectRequest req,
        UriComponentsBuilder ucb){

            //Busca el cliente por ID
            Client client = projectService.findClientById(req.clientId());
            if(client == null){
                return ResponseEntity.badRequest().build();
            }

            var entity = Project.builder()
                    .name(req.name().trim())
                    .description(req.description())
                    .startDate(req.startDate())
                    .endDate(req.endDate())
                    .active(Boolean.TRUE.equals(req.active()))
                    .client(client)
                    .build();

            var saved = projectService.saveProject(entity);
            var location = ucb.path("/api/projects/{id}").buildAndExpand(saved.getId()).toUri();

            var dto = new ProjectDto(
                saved.getId(),
                saved.getName(),
                saved.getDescription(),
                saved.getStartDate(),
                saved.getEndDate(),
                saved.getActive(),
                saved.getClient() != null ? saved.getClient().getId() : null
            );

            return ResponseEntity.created(location).body(dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id){
            projectService.deleteById(id);
        return ResponseEntity.noContent().build();
    }



}
