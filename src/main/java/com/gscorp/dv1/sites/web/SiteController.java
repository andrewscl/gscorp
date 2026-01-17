package com.gscorp.dv1.sites.web;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gscorp.dv1.clients.application.ClientService;
import com.gscorp.dv1.clients.web.dto.ClientSelectDto;
import com.gscorp.dv1.projects.application.ProjectService;
import com.gscorp.dv1.sites.application.SiteService;
import com.gscorp.dv1.sites.web.dto.SiteDtoProjection;
import com.gscorp.dv1.users.application.UserService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/private/sites")
@RequiredArgsConstructor
public class SiteController {

    private final UserService userService;
    private final SiteService siteService;
    private final ProjectService projectService;
    private final ClientService clientService;

    private String googleCloudApiKey = System.getenv("GOOGLE_CLOUD_API_KEY");

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
        return "private/sites/views/view-site-view";
    }

    @GetMapping("/edit/{id}")
    public String editSite (@PathVariable Long id, Model model){
        var site = siteService.findByIdWithProjects(id);
        model.addAttribute("site", site);
        model.addAttribute("googlecloudapikey", googleCloudApiKey);
        return "private/sites/views/edit-site-view";
    }

    @GetMapping("/set-coordinates")
    public String setSiteCoordinates(Model model) {
        model.addAttribute("sites", siteService.getAllSites());
        model.addAttribute("googlecloudapikey", googleCloudApiKey);
        return "private/sites/views/set-site-coordinates-view";
    }

    @GetMapping("/sites-map")
    public String getSitesMap(
            Model model,
            Authentication authentication) {

            Long userId = userService.getUserIdFromAuthentication(authentication);

            List<ClientSelectDto> clientDtos = clientService.findClientsByUserId(userId);

            List<SiteDtoProjection> siteProjections =
                                        siteService.findSiteProjectionsByClientIds(clientDtos.stream()
                                                                        .map(ClientSelectDto::id)
                                                                        .toList());

        model.addAttribute("sites", siteProjections);
        model.addAttribute("googlecloudapikey", googleCloudApiKey);
        return "private/sites/views/sites-map-view";
    }

}
