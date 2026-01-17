package com.gscorp.dv1.sites.web;

import java.util.List;
import java.util.TimeZone;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.gscorp.dv1.clients.application.ClientService;
import com.gscorp.dv1.clients.web.dto.ClientSelectDto;
import com.gscorp.dv1.projects.application.ProjectService;
import com.gscorp.dv1.projects.infrastructure.Project;
import com.gscorp.dv1.sites.application.SiteService;
import com.gscorp.dv1.sites.infrastructure.Site;
import com.gscorp.dv1.sites.web.dto.CreateSiteRequest;
import com.gscorp.dv1.sites.web.dto.SetSiteCoordinatesDto;
import com.gscorp.dv1.sites.web.dto.SiteDto;
import com.gscorp.dv1.sites.web.dto.SiteDtoProjection;
import com.gscorp.dv1.sites.web.dto.UpdateLatLon;
import com.gscorp.dv1.users.application.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/sites")
@RequiredArgsConstructor
public class SiteRestController {

    private final SiteService siteService;
    private final ProjectService projectService;
    private final UserService userService;
    private final ClientService clientService;


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
                saved.getAddress(),
                saved.getTimeZone(),
                saved.getLat(),
                saved.getLon(),
                saved.getActive());
        return ResponseEntity.created(location).body(dto);

        }


        //Borrar site
        @DeleteMapping("/{id}")
        public ResponseEntity<Void> delete(@PathVariable Long id){
                siteService.deleteById(id);
                return ResponseEntity.noContent().build();
        }


        //Actualizar latitud y longitud
        @PutMapping("/update-location/{id}")
        public ResponseEntity<Site> updateSiteLocation(@PathVariable Long id,
        @RequestBody UpdateLatLon updateLatLon){
               Site updateLocation = siteService.updateSiteLocation(id, updateLatLon);
                return ResponseEntity.ok(updateLocation);
        }

        @PutMapping("/update/{id}")
        public ResponseEntity<?> updateSite(@PathVariable Long id, @RequestBody SiteDto in) {
                try {
                        SiteDto updated = siteService.updateSite(id, in);
                        return ResponseEntity.ok(updated);
                } catch (Exception e) {
                        e.printStackTrace();
                        return ResponseEntity.badRequest().body("No se pudo guardar el sitio: " + e.getMessage());
                }
        }

        @PostMapping("/set-coordinates")
        public ResponseEntity<SetSiteCoordinatesDto> setSiteCoordinates(@RequestBody SetSiteCoordinatesDto dto) {
        SetSiteCoordinatesDto updated = siteService.setCoordinates(dto.siteId(), dto.latitude(), dto.longitude());
        return ResponseEntity.ok(updated);
        }


        @GetMapping("/projections-by-user")
        public ResponseEntity<List<SiteDtoProjection>> getSiteProjectionsByUser(
                Authentication authentication
        ) {
                Long userId = userService.getUserIdFromAuthentication(authentication);

                List<ClientSelectDto> clientDtos = clientService.findClientsByUserId(userId);

                List<SiteDtoProjection> siteProjections = siteService.findSiteProjectionsByClientIds(clientDtos.stream()
                        .map(ClientSelectDto::id)
                        .toList());

                return ResponseEntity.ok(siteProjections);

        }

}
