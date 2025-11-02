package com.gscorp.dv1.sitesupervisionvisits.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.gscorp.dv1.sites.application.SiteService;
import com.gscorp.dv1.sites.web.dto.SiteDto;
import com.gscorp.dv1.sitesupervisionvisits.application.SiteSupervisionVisitService;
import com.gscorp.dv1.sitesupervisionvisits.web.dto.CreateSiteSupervisionVisitRequest;
import com.gscorp.dv1.sitesupervisionvisits.web.dto.SiteSupervisionVisitDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/site-supervision-visits")
@RequiredArgsConstructor
public class SiteSupervisionVisitRestController {

    private final SiteService siteService;

    private final SiteSupervisionVisitService siteSupervisionVisitService;

    @Value("${file.upload-dir}")
    private String uploadDir;
    
    @GetMapping("/sites")
    public List<SiteDto> getSitesApi() {
        return siteService.getAllSites();
    }

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SiteSupervisionVisitDto> createSiteSupervisionVisit(
        @Valid @ModelAttribute CreateSiteSupervisionVisitRequest req,
        UriComponentsBuilder ucb) {

        SiteSupervisionVisitDto saved =
            siteSupervisionVisitService.
                        createSiteSupervisionVisitRequest(req);

        var location = ucb.path("/api/site-supervision-visits/{id}")
                                    .buildAndExpand(saved.id()).toUri();

        return ResponseEntity.created(location).body(saved);
    }

}