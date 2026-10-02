package com.gscorp.dv1.admin.projects.web;

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

import com.gscorp.dv1.admin.clients.infrastructure.Client;
import com.gscorp.dv1.admin.projects.application.ProjectService;
import com.gscorp.dv1.admin.projects.infrastructure.Project;
import com.gscorp.dv1.admin.projects.web.dto.CreateProjectRequest;
import com.gscorp.dv1.admin.projects.web.dto.ProjectDto;
import com.gscorp.dv1.operations.sites.application.SiteService;
import com.gscorp.dv1.operations.sites.web.dto.SiteDto;

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
                saved.getExternalId(),
                saved.getName(),
                saved.getClient().getName(),
                saved.getDescription(),
                saved.getStartDate(),
                saved.getEndDate(),
                saved.getStatus(),
                saved.getActive()
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
            List<SiteDto> sites = siteService.findDtosByProjectId(projectId);
            return ResponseEntity.ok(sites);
    }

}
