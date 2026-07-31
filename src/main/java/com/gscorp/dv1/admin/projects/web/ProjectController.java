package com.gscorp.dv1.admin.projects.web;

import java.util.UUID;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gscorp.dv1.admin.projects.application.ProjectService;
import com.gscorp.dv1.config.security.SecurityUser;

import lombok.AllArgsConstructor;

@Controller
@RequestMapping("/private/projects")
@AllArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    
    @GetMapping("/list")
    public String getProjectsTableView (
                Model model,
                @AuthenticationPrincipal SecurityUser securityUser
    ) {
        if(securityUser == null) return "redirect:/login";
        UUID externalId = securityUser.getUser().getExternalId();
        model.addAttribute("projects",
                    projectService.findByUserExternalId(externalId));
        return "private/admin/projects/projects-list";
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
