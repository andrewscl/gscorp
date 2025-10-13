package com.gscorp.dv1.sites.application;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.gscorp.dv1.sites.infrastructure.Site;
import com.gscorp.dv1.sites.web.dto.SiteDto;

@Service
public interface SiteService {
    Site saveSite (Site site);
    void deleteById (Long id);
    Site findById (Long id);
    List<SiteDto> getAllSites();
    Site findByIdWithProjects(Long id);
}
