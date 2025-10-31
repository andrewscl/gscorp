package com.gscorp.dv1.sitesupervisionvisits.web;

import java.io.File;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import com.gscorp.dv1.sites.application.SiteService;
import com.gscorp.dv1.sites.web.dto.SiteDto;
import com.gscorp.dv1.sitesupervisionvisits.application.SiteSupervisionVisitService;
import com.gscorp.dv1.sitesupervisionvisits.web.dto.CreateSiteSupervisionVisitDto;
import com.gscorp.dv1.sitesupervisionvisits.web.dto.SiteSupervisionVisitDto;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/site-supervision-visits")
@RequiredArgsConstructor
public class SiteSupervisionVisitRestController {

/*     private final SiteService siteService;

    private final SiteSupervisionVisitService siteSupervisionVisitService;

    @Value("${file.upload-dir}")
    private String uploadDir;
    
    @GetMapping("/sites")
    public List<SiteDto> getSitesApi() {
        return siteService.getAllSites();
    }
 
    @PostMapping("/create")
    public ResponseEntity<SiteSupervisionVisitDto> createSiteSupervisionVisit(
        @RequestParam Long siteId,
        @RequestParam Double latitude,
        @RequestParam Double longitude,
        @RequestParam String description,
        @RequestParam(required = false) MultipartFile photo,
        @RequestParam(required = false) MultipartFile video,
        UriComponentsBuilder ucb) {

        // 1. Guarda los archivos y obtén las rutas
        String photoPath = null;
        String videoPath = null;
        try {
            if (photo != null && !photo.isEmpty()) {
                String filename = UUID.randomUUID() + "_" + photo.getOriginalFilename();
                File dest = new
                    File(uploadDir + File.separator + "photos" + File.separator + filename);
                dest.getParentFile().mkdirs();
                photo.transferTo(dest);
                photoPath = "uploads/photos/" + filename;
            }

            if (video != null && !video.isEmpty()) {
                String filename = UUID.randomUUID() + "_" + video.getOriginalFilename();
                File dest = new
                    File(uploadDir + File.separator + "videos" + File.separator + filename);
                dest.getParentFile().mkdirs();
                video.transferTo(dest);
                videoPath = "uploads/videos/" + filename;
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }

        // 2. Arma el DTO para el service con las rutas
        CreateSiteSupervisionVisitDto req = new CreateSiteSupervisionVisitDto(
            siteId, latitude, longitude, description, photoPath, videoPath
        );
        SiteSupervisionVisitDto savedDto = siteSupervisionVisitService.saveSiteSupervisionVisit(req);

        var location = ucb.path("/api/site-supervision-visits/{id}")
                .buildAndExpand(savedDto.id()).toUri();

        return ResponseEntity.created(location).body(savedDto);
    }*/

}
