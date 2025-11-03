package com.gscorp.dv1.patrol.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gscorp.dv1.sites.application.SiteService;

import lombok.AllArgsConstructor;

@Controller
@RequestMapping("/private/patrol-checkpoints")
@AllArgsConstructor
public class PatrolCheckpointController {

    private final SiteService siteService;

    @GetMapping("/table-view")
    public String getPatrolCheckpointsTableView (Model model) {
        model.addAttribute("sites",
                    siteService.getAllSites());
        return "private/patrols/views/patrol-checkpoints-table-view";
    }


}
