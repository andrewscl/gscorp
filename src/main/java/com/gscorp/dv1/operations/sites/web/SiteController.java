package com.gscorp.dv1.operations.sites.web;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

import com.gscorp.dv1.admin.projects.application.ProjectService;
import com.gscorp.dv1.config.security.SecurityUser;
import com.gscorp.dv1.operations.sites.application.SiteService;
import com.gscorp.dv1.operations.sites.web.dto.SiteDto;
import com.gscorp.dv1.users.application.UserScopeService;
import com.gscorp.dv1.users.application.dto.ProjectScope;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/private/sites")
@RequiredArgsConstructor
public class SiteController {

    private final SiteService siteService;
    private final ProjectService projectService;
    private final UserScopeService userScopeService;

    private String googleCloudApiKey = System.getenv("GOOGLE_CLOUD_API_KEY");
    private String googleMapId = System.getenv("GOOGLE_MAP_ID");

    @GetMapping("/table-view")
    public String getSitesTableView(
                    Model model,
                    @AuthenticationPrincipal SecurityUser securityUser) {
        if(securityUser == null) return "redirect:/login";
        UUID externalId = securityUser.getUser().getExternalId();
        model.addAttribute("sites",
                                            siteService.getAllSitesByUser(externalId));
        model.addAttribute("projects",
                        projectService.findAllWithClientsAndEmployees());
        return "private/sites/views/sites-list";
    }

    @GetMapping("/create")
    public String createSite(Model model) {
        model.addAttribute("projects", projectService.findAllWithClientsAndEmployees());
        model.addAttribute("googlecloudapikey", googleCloudApiKey);
        model.addAttribute("googlemapid", googleMapId);
        return "private/sites/fragments/create-site";
    }

    @GetMapping("/show/{id}")
    public String showSite (@PathVariable Long id, Model model){
        var site = siteService.findByIdWithProjects(id);
        model.addAttribute("site", site);
        model.addAttribute("googlecloudapikey", googleCloudApiKey);
        model.addAttribute("googlemapid", googleMapId);
        return "private/sites/fragments/view-site";
    }

    @GetMapping("/edit/{siteExternalId}")
    public String editSite (
                    @PathVariable UUID siteExternalId,
                    @AuthenticationPrincipal SecurityUser securityUser,                    
                    Model model){
        ProjectScope scope = userScopeService.getProjectScope();
        var siteOpt = siteService.findDtoByExternalId(
                                scope.ignoreFilter(),
                                scope.projectIds(),
                                siteExternalId);
        SiteDto site = siteOpt.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                    "El sitio no existe o no tienes acceso"));
        model.addAttribute("site", site);
        model.addAttribute("googlecloudapikey", googleCloudApiKey);
        model.addAttribute("googlemapid", googleMapId);
        return "private/sites/fragments/edit-site";
    }

    @GetMapping("/set-coordinates")
    public String setSiteCoordinates(Model model) {
        model.addAttribute("sites", siteService.getAllSites());
        model.addAttribute("googlecloudapikey", googleCloudApiKey);
        model.addAttribute("googlemapid", googleMapId);
        return "private/sites/views/set-site-coordinates-view";
    }

    @GetMapping("/sites-map")
    public String getSitesMap(
            Model model) {

        if (googleCloudApiKey == null || googleMapId == null) {
            throw new IllegalStateException("Faltan valores en Google Maps API Key o Map ID.");
        }

        System.out.println("Google API Key: " + googleCloudApiKey);
        System.out.println("Google Map ID: " + googleMapId);

        model.addAttribute("googlecloudapikey", googleCloudApiKey);
        model.addAttribute("googlemapid", googleMapId);
        return "private/sites/views/sites-map-view";
    }

}
