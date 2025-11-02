package com.gscorp.dv1.sitesupervisionvisits.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gscorp.dv1.sites.application.SiteService;
//import com.gscorp.dv1.sitesupervisionvisits.application.SiteSupervisionVisitService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/private/site-supervision-visits")
@RequiredArgsConstructor
public class SiteSupervisionVisitController {

    private final SiteService siteService;
    private final String googleCloudApiKey = System.getenv("GOOGLE_CLOUD_API_KEY");
    //private final SiteSupervisionVisitService siteSupervisionVisitService;

    @GetMapping("/table-view")
    public String getSiteSupervisionVisitsTableView(Model model) {
        model.addAttribute("sites", siteService.getAllSites());
        model.addAttribute("googlecloudapikey", googleCloudApiKey);
        //model.addAttribute("siteSupervisionVisits", siteSupervisionVisitService.getAllSiteSupervisionVisits());
        return "private/site-supervision-visits/views/site-supervision-visit-table-view";
    }

    @GetMapping("/create")
    public String createVisit(Model model) {
        model.addAttribute("sites", siteService.getAllSites());
        model.addAttribute("googlecloudapikey", googleCloudApiKey);
        return "private/site-supervision-visits/views/create-site-sup-visit-view";
    }



}
