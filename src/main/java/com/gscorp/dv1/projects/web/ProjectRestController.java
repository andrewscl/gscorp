package com.gscorp.dv1.projects.web;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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
import com.gscorp.dv1.sites.application.SiteService;
import com.gscorp.dv1.sites.web.dto.SiteSelectDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectRestController {
    
    private final ProjectService projectService;
    private final SiteService siteService;

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


    @GetMapping("/{projectId}/sites")
    public ResponseEntity<?> findSitesByProject(@PathVariable("projectId") Long projectId) {
        try {
            log.debug("GET /api/projects/{}/sites", projectId);
            if (projectId == null) {
                return ResponseEntity.badRequest().body(java.util.Map.of("message", "projectId requerido"));
            }
            List<SiteSelectDto> sites = siteService.findSelectDtoByProjectId(projectId);
            return ResponseEntity.ok(sites);
        } catch (Exception ex) {
            log.error("Error fetching sites for project {}: {}", projectId, ex.getMessage(), ex);
            return ResponseEntity.status(500)
                    .body(java.util.Map.of("message", "Error interno cargando sites", "detail", ex.getMessage()));
        }
    }

}
