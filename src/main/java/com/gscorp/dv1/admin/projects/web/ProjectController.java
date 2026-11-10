package com.gscorp.dv1.admin.projects.web;

import java.util.UUID;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gscorp.dv1.admin.companies.application.CompanyService;
import com.gscorp.dv1.admin.projects.application.ProjectService;
import com.gscorp.dv1.config.security.SecurityUser;
import com.gscorp.dv1.users.application.UserScopeService;
import com.gscorp.dv1.users.application.dto.ProjectScope;

import lombok.AllArgsConstructor;

@Controller
@RequestMapping("/private/projects")
@AllArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    private final UserScopeService userScopeService;
    private final CompanyService companyService;

    @GetMapping("/create")
    public String createProject(
            Model model,
            @AuthenticationPrincipal SecurityUser securityUser) {
        model.addAttribute("companies", 
                            companyService.findCompaniesForCompanyCreation(securityUser));
        return "private/admin/projects/create-project";
    }

    @GetMapping("/list")
    public String getProjectsTableView (
                Model model) {
        ProjectScope scope = userScopeService.getProjectScope();
        model.addAttribute("projects",
                                    projectService.findByUserScope(
                                        scope.ignoreFilter(),
                                        scope.projectIds(),
                                        null
                                    ));
        return "private/admin/projects/projects-list";
    }

    @GetMapping("/show/{externalId}")
    public String showProject (@PathVariable("externalId") UUID externalId, Model model){
        var project = projectService.getProjectDtoByExternalId(externalId);
        model.addAttribute("project", project);
        return "private/admin/projects/view-project";
    }

    @GetMapping("/edit/{externalId}")
    public String editProject (@PathVariable("externalId") UUID externalId, Model model){
        var project = projectService.getProjectDtoByExternalId(externalId);
        model.addAttribute("project", project);
        return "private/admin/projects/edit-project";
    }

}
