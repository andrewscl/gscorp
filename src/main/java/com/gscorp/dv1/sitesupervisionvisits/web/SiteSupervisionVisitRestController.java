package com.gscorp.dv1.sitesupervisionvisits.web;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.gscorp.dv1.sites.application.SiteService;
import com.gscorp.dv1.sites.web.dto.SiteDto;
import com.gscorp.dv1.sitesupervisionvisits.application.SiteSupervisionVisitService;
import com.gscorp.dv1.sitesupervisionvisits.web.dto.CreateSiteSupervisionVisitDto;
import com.gscorp.dv1.sitesupervisionvisits.web.dto.SiteSupervisionVisitDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/site-supervision-visits")
@RequiredArgsConstructor
public class SiteSupervisionVisitRestController {

    private final SiteService siteService;

    private final SiteSupervisionVisitService siteSupervisionVisitService;
    
    @GetMapping("/sites")
    public List<SiteDto> getSitesApi() {
        return siteService.getAllSites();
    }

    @PostMapping("/create")
    public ResponseEntity<SiteSupervisionVisitDto> createSiteSupervisionVisit(
        @Valid @RequestBody CreateSiteSupervisionVisitDto req,
        UriComponentsBuilder ucb) {

        SiteSupervisionVisitDto savedDto = siteSupervisionVisitService
            .saveSiteSupervisionVisit(req);

        //Respuesta
        var location = ucb.path("/api/site-supervision-visits/{id}")
            .buildAndExpand(savedDto.id()).toUri();

        return ResponseEntity.created(location).body(savedDto);
    }

}
