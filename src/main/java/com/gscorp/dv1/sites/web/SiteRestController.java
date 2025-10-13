package com.gscorp.dv1.sites.web;

import java.util.TimeZone;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.gscorp.dv1.projects.application.ProjectService;
import com.gscorp.dv1.projects.infrastructure.Project;
import com.gscorp.dv1.sites.application.SiteService;
import com.gscorp.dv1.sites.infrastructure.Site;
import com.gscorp.dv1.sites.web.dto.CreateSiteRequest;
import com.gscorp.dv1.sites.web.dto.SiteDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/sites")
@RequiredArgsConstructor
public class SiteRestController {

    private final SiteService siteService;
    private final ProjectService projectService;

    @PostMapping("/create")
    public ResponseEntity <SiteDto> createSite(
        @Valid @RequestBody CreateSiteRequest req,
        UriComponentsBuilder ucb){
        
        //Resolver proyecto
        Long projectId = req.projectId();
        Project project = projectService.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Proyecto no encontrado: " + projectId));

        //Normalizar campos opcionales
        String tz = (req.timeZone() == null || req.timeZone().isBlank())
                ? TimeZone.getDefault().getID()
                : req.timeZone().trim();

        Double lat = req.lat();  //Puede ser null
        Double lon = req.lon();    //Puede ser null

        //Construir entidad
        var entity = Site.builder()
            .project(project)
            .name(req.name().trim())
            .code(req.code())
            .address(req.address())
            .lat(lat)
            .lon(lon)
            .timeZone(tz)
            .active(req.active() == null ? true : req.active())
            .build();
        
        //Guardar
        var saved = siteService.saveSite(entity);

        //Respuesta
        var location = ucb.path("/api/sites/{id}").buildAndExpand(saved.getId()).toUri();

        var dto = new SiteDto(
                saved.getId(),
                saved.getProject().getId(),
                saved.getProject().getName(),
                saved.getName(),
                saved.getCode(),
                saved.getAddress(),
                saved.getTimeZone(),
                saved.getActive());
        return ResponseEntity.created(location).body(dto);

        }

        //Borrar site
        @DeleteMapping("/{id}")
        public ResponseEntity<Void> delete(@PathVariable Long id){
                siteService.deleteById(id);
                return ResponseEntity.noContent().build();
        }

}
