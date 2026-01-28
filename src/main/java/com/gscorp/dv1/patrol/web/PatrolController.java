package com.gscorp.dv1.patrol.web;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gscorp.dv1.enums.DayOfWeek;
import com.gscorp.dv1.patrol.application.PatrolService;
import com.gscorp.dv1.patrol.web.dto.PatrolDto;
import com.gscorp.dv1.sites.application.SiteService;
import com.gscorp.dv1.sites.web.dto.SiteDtoProjection;
import com.gscorp.dv1.users.application.UserService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/private/patrols")
@RequiredArgsConstructor
public class PatrolController {

    private final UserService userService;
    private final PatrolService patrolService;
    private final SiteService siteService;
    
    @GetMapping("/table-view")
    public String getPatrolsTableView(
                    Model model,
                    Authentication authentication) {

        Long userId = userService.getUserIdFromAuthentication(authentication);
        if(userId == null) {
            return "redirect:/login";
        }

        List<PatrolDto> patrols = patrolService.getPatrolsByUserId(userId);
        if(patrols == null || patrols.isEmpty()) {
            model.addAttribute("infoMessage", 
                "No se encontraron rondas de supervisión asociadas a su usuario.");
        } else {
            model.addAttribute("patrols", patrols);
        }

        
        return "private/patrols/views/patrols-table-view";
    }

    @GetMapping("/create")
    public String getCreatePatrolForm (
                    Model model,
                    Authentication authentication) {

        Long userId = userService.getUserIdFromAuthentication(authentication);
        if(userId == null) {
            return "redirect:/login";
        }

        List<SiteDtoProjection> sites = siteService.findSiteProjectionsByUserId(userId);

        model.addAttribute("DayOfWeek", DayOfWeek.values());
        model.addAttribute("siteList", sites);
        return "private/patrols/views/create-patrol-view";
    }


}
