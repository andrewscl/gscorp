package com.gscorp.dv1.sitesupervisionvisits.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.gscorp.dv1.sites.application.SiteService;
import com.gscorp.dv1.sites.web.dto.SiteDto;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/site-supervision-visits")
@RequiredArgsConstructor
public class SiteSupervisionVisitRestController {

    private final SiteService siteService;
    
    @GetMapping("/sites")
    @ResponseBody
    public List<SiteDto> getSitesApi() {
        return siteService.getAllSites();
    }

}
