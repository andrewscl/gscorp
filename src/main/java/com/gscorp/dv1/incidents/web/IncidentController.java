package com.gscorp.dv1.incidents.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gscorp.dv1.incidents.application.IncidentService;

import lombok.AllArgsConstructor;

@Controller
@RequestMapping("/private/incidents")
@AllArgsConstructor
public class IncidentController {

    private final IncidentService incidentService;
    
    @GetMapping("/table-view")
    public String getIncidentsTableView(Model model) {
        model.addAttribute("incidents", incidentService.findAll());
        return "private/incidents/views/incidents-table-view";
    }

    @GetMapping("/create")
    public String createIncident(Model model) {
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
