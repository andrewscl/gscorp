package com.gscorp.dv1.incidents.web;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gscorp.dv1.clients.application.ClientService;
import com.gscorp.dv1.enums.IncidentType;
import com.gscorp.dv1.enums.Priority;
import com.gscorp.dv1.incidents.application.IncidentService;
import com.gscorp.dv1.sites.application.SiteService;
import com.gscorp.dv1.sites.web.dto.SiteSelectDto;
import com.gscorp.dv1.users.application.UserService;

import lombok.AllArgsConstructor;

@Controller
@RequestMapping("/private/incidents")
@AllArgsConstructor
public class IncidentController {

    private final IncidentService incidentService;
    private final SiteService siteService;
    private final UserService userService;
    private final ClientService clientService;

    @GetMapping("/table-view")
    public String getIncidentsTableView(Model model) {
        model.addAttribute("incidents", incidentService.findAll());
        return "private/incidents/views/incidents-table-view";
    }

    @GetMapping("/create")
    public String createIncident(Model model, Authentication authentication) {
        Long userId = userService.getUserIdFromAuthentication(authentication);
        List<Long> clientIds = clientService.getClientIdsByUserId(userId);
        List<SiteSelectDto> sites = siteService.getAllSitesForClients(clientIds);
        model.addAttribute("sites", sites);
        model.addAttribute("incidentTypes", IncidentType.values());
        model.addAttribute("priorities", Priority.values());
        return "private/incidents/views/create-incident-view";ok
    }

    @GetMapping("/show/{id}")
    public String showIncident(@PathVariable Long id, Model model){
        var incident = incidentService.findById(id);
        model.addAttribute("incident", incident);
        return "private/incidents/views/view-incident-view";
    }

    @GetMapping("/edit/{id}")
    public String editIncident(@PathVariable Long id, Model model){
        var incident = incidentService.findById(id);
        model.addAttribute("incident", incident);
        return "private/incidents/views/edit-incident-view";
    }
}
