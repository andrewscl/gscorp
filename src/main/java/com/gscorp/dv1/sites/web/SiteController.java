package com.gscorp.dv1.sites.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gscorp.dv1.sites.application.SiteService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/private/sites")
@RequiredArgsConstructor
public class SiteController {

    private final SiteService siteService;

    @GetMapping("/table-view")
    public String getSitesTableView(Model model) {
        model.addAttribute("sites", siteService.getAllSites());
        return "private/sites/views/sites-table-view";
    }

     @GetMapping("/show/{id}")
    public String showSite (@PathVariable Long id, Model model){
        var site = siteService.findByIdWithClients(id);
        model.addAttribute("site", site);
        return "private/sites/views/view-site-view";
    }

    @GetMapping("/edit/{id}")
    public String editSite (@PathVariable Long id, Model model){
        var site = siteService.findByIdWithClients(id);
        model.addAttribute("site", site);
        return "private/sites/views/edit-site-view";
    }

}
