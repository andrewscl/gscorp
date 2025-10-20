package com.gscorp.dv1.sites.application;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.gscorp.dv1.sites.infrastructure.Site;
import com.gscorp.dv1.sites.web.dto.SiteDto;
import com.gscorp.dv1.sites.web.dto.UpdateLatLon;

@Service
public interface SiteService {
    Site saveSite (Site site);
    void deleteById (Long id);
    Optional<Site> findById (Long id);
    List<SiteDto> getAllSites();
    Site findByIdWithProjects(Long id);
    Site updateSiteLocation(Long id, UpdateLatLon updateLatLon);
    SiteDto updateSite(Long id, SiteDto siteDto);
}
