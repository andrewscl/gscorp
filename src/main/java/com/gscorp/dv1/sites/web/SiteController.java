package com.gscorp.dv1.sites.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gscorp.dv1.projects.application.ProjectService;
import com.gscorp.dv1.sites.application.SiteService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/private/sites")
@RequiredArgsConstructor
public class SiteController {

    private final SiteService siteService;
    private final ProjectService projectService;

    private String googleCloudApiKey = System.getenv("GOOGLE_CLOUD_API_KEY");
    private String googleMapId = System.getenv("GOOGLE_MAP_ID");

    @GetMapping("/table-view")
    public String getSitesTableView(Model model) {
        model.addAttribute("sites", siteService.getAllSites());
        model.addAttribute("projects", projectService.findAllWithClientsAndEmployees());
        return "private/sites/views/sites-table-view";
    }

    @GetMapping("/show/{id}")
    public String showSite (@PathVariable Long id, Model model){
        var site = siteService.findByIdWithProjects(id);
        model.addAttribute("site", site);
        model.addAttribute("googlecloudapikey", googleCloudApiKey);
        model.addAttribute("googlemapid", googleMapId);
        return "private/sites/views/view-site-view";
    }

    @GetMapping("/edit/{id}")
    public String editSite (@PathVariable Long id, Model model){
        var site = siteService.findByIdWithProjects(id);
        model.addAttribute("site", site);
        model.addAttribute("googlecloudapikey", googleCloudApiKey);
        model.addAttribute("googlemapid", googleMapId);
        return "private/sites/views/edit-site-view";
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
