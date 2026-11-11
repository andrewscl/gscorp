package com.gscorp.dv1.admin.projects.web;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.gscorp.dv1.admin.projects.application.ProjectService;
import com.gscorp.dv1.admin.projects.web.dto.CreateProjectRequest;
import com.gscorp.dv1.admin.projects.web.dto.ProjectDto;
import com.gscorp.dv1.operations.sites.application.SiteService;
import com.gscorp.dv1.operations.sites.web.dto.SiteDto;

import jakarta.validation.Valid;
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
                @Valid @RequestBody CreateProjectRequest req,
                UriComponentsBuilder ucb
            ){

            var saved = projectService.createProject(req);
            var location = ucb.path("/api/projects/{externalId}")
                            .buildAndExpand(saved.externalId())
                            .toUri();

            return ResponseEntity.created(location).body(saved);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id){
            projectService.deleteById(id);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/{projectExternalId}/sites")
    public ResponseEntity<?> findSitesByProject(@PathVariable("projectExternalId") UUID projectExternalId) {
        List<SiteDto> sites = siteService.findDtoByUserExternalId(null);
        return ResponseEntity.ok(sites);
    }

    @GetMapping("/company/{companyExternalId}/projects")
    public ResponseEntity<List<ProjectDto>> findByCompany(@PathVariable("companyExternalId") UUID companyExternalId){
        List<ProjectDto> projects = projectService.findDtosByCompanyExternalId(companyExternalId);
        return ResponseEntity.ok(projects);
    }

}
