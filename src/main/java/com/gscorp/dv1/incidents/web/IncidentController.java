package com.gscorp.dv1.incidents.web;

import java.util.List;
import java.util.UUID;

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
import com.gscorp.dv1.incidents.web.dto.IncidentDto;
import com.gscorp.dv1.security.SecurityUser;
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
    public String getIncidentsTableView(
                            Model model, 
                            Authentication authentication) {

        Long userId = userService.getUserIdFromAuthentication(authentication);

            Object principal = authentication.getPrincipal();
            if(!(principal instanceof SecurityUser)) {
                return "redirect:/login";
            }

            SecurityUser securityUser = (SecurityUser) principal;

            UUID externalId = securityUser.getUser().getExternalId();

        List<Long> clientIds = clientService.getClientIdsByUserExternalId(externalId);

        //Si no tiene clientes asociados, retornar vista vacia
        List<IncidentDto> incidents = clientIds == null || clientIds.isEmpty()
                                        ? List.of()
                                        : incidentService.findIncidentsForUser(userId);

        model.addAttribute("incidents", incidents);
        return "private/incidents/views/incidents-table-view";
    }



    @GetMapping("/create")
    public String createIncident(
                        Model model,
                        Authentication authentication) {

            Object principal = authentication.getPrincipal();
            if(!(principal instanceof SecurityUser)) {
                return "redirect:/login";
            }

            SecurityUser securityUser = (SecurityUser) principal;

            UUID externalId = securityUser.getUser().getExternalId();

        List<Long> clientIds = clientService.getClientIdsByUserExternalId(externalId);
        List<SiteSelectDto> sites = siteService.getAllSitesForClients(clientIds);

        model.addAttribute("sites", sites);
        model.addAttribute("incidentTypes", IncidentType.values());
        model.addAttribute("priorities", Priority.values());
        return "private/incidents/views/create-incident-view";
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
