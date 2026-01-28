package com.gscorp.dv1.sites.application;

import java.util.List;
import java.util.Optional;

import com.gscorp.dv1.sites.infrastructure.Site;
import com.gscorp.dv1.sites.web.dto.SetSiteCoordinatesDto;
import com.gscorp.dv1.sites.web.dto.SiteDto;
import com.gscorp.dv1.sites.web.dto.SiteDtoProjection;
import com.gscorp.dv1.sites.web.dto.SiteSelectDto;
import com.gscorp.dv1.sites.web.dto.UpdateLatLon;

public interface SiteService {

    Site saveSite (Site site);

    void deleteById (Long id);

    Optional<Site> findById (Long id);

    List<SiteDto> getAllSites();

    Site findByIdWithProjects(Long id);

    Site updateSiteLocation(Long id, UpdateLatLon updateLatLon);

    SiteDto updateSite(Long id, SiteDto siteDto);

    SetSiteCoordinatesDto setCoordinates(Long siteId, Double latitude, Double longitude);

    List<SiteSelectDto> getAllSitesForClients(List<Long> clientIds);

    Optional<Long> getClientIdForSite(Long siteId);

    List<SiteSelectDto> findSelectDtoByProjectId(Long projectId);

    List<SiteSelectDto> findByUserId(Long userId);

    SiteSelectDto findNearestSite(Long userId, double lat, double lon);

    double haversineMeters(double lat1,double lon1,double lat2,double lon2);

    List<SiteDtoProjection> findSiteProjectionsByClientIds(List<Long> clientIds);

    List<SiteDtoProjection> findSiteProjectionsByUserId(Long userId);

}
