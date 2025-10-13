package com.gscorp.dv1.projects.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gscorp.dv1.projects.infrastructure.application.ProjectService;

import lombok.AllArgsConstructor;

@Controller
@RequestMapping("/private/projects")
@AllArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    
    @GetMapping("/table-view")
    public String getProjectsTableView (Model model) {
        model.addAttribute("projects",
                    projectService.findAll());
        return "private/projects/views/projects-table-view";
    }

    @GetMapping("/show/{id}")
    public String showProject (@PathVariable Long id, Model model){
        var project = projectService.findByIdWithClients(id);
        model.addAttribute("project", project);
        return "private/projects/views/view-project-view";
    }

    @GetMapping("/edit/{id}")
    public String editProject (@PathVariable Long id, Model model){
        var project = projectService.findByIdWithClients(id);
        model.addAttribute("project", project);
        return "private/projects/views/edit-project-view";
    }

}
