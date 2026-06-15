package com.gscorp.dv1.patrolexecution.web;

import java.util.List;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gscorp.dv1.security.SecurityUser;
import com.gscorp.dv1.sites.application.SiteService;
import com.gscorp.dv1.sites.web.dto.SiteDto;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/private/patrol-executions")
@RequiredArgsConstructor
public class PatrolExecutionController {

    private final SiteService siteService;

    @GetMapping("/execution-view")
    public String getAttendanceView (
            Model model,
            Authentication authentication){

        if(authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        Object principal = authentication.getPrincipal();
        if(!(principal instanceof SecurityUser)) {
            return "redirect:/login";
        }

        SecurityUser securityUser = (SecurityUser) principal;
        UUID externalId = securityUser.getUser().getExternalId();
        List<SiteDto> sites = siteService.getAllSitesByUser(externalId);

        model.addAttribute("sites", sites);
        return "private/patrol-executions/views/patrol-execution-view";
    }


}
