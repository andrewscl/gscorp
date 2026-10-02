package com.gscorp.dv1.operations.sites.application;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.gscorp.dv1.operations.sites.infrastructure.Site;
import com.gscorp.dv1.operations.sites.web.dto.SetSiteCoordinatesDto;
import com.gscorp.dv1.operations.sites.web.dto.SiteDto;
import com.gscorp.dv1.operations.sites.web.dto.UpdateLatLon;
import com.gscorp.dv1.operations.sites.web.dto.UpdateSiteRequest;

public interface SiteService {

    Site saveSite (Site site);

    void deleteById (Long id);

    Optional<Site> findById (Long id);

    Optional<Site> findByExternalId (
                    boolean ignoreProjectFilter,
                    List<Long> projectsIds,
                    UUID externalId);

    List<SiteDto> getAllSites();

    List<SiteDto> getAllSitesByUser(UUID userExternalId);

    Site findByIdWithProjects(Long id);

    Site updateSiteLocation(Long id, UpdateLatLon updateLatLon);

    SiteDto updateSite(
                    boolean ignoreProjectFilter,
                    List<Long> projectIds,
                    UUID externalId,
                    UpdateSiteRequest request);

    SetSiteCoordinatesDto setCoordinates(Long siteId, Double latitude, Double longitude);

    List<SiteDto> getAllSitesForClients(List<Long> clientIds);

    Optional<Long> getClientIdForSite(Long siteId);

    List<SiteDto> findDtosByProjectId(Long projectId);

    List<SiteDto> findByUserScope(boolean ignoreProjectFilter,
                                List<Long> projectIds);

    SiteDto findNearestSite(boolean ignoreProjectFilter,
                            List<Long> projectIds,
                            double lat,
                            double lon);

    double haversineMeters(double lat1, 
                            double lon1,
                            double lat2,
                            double lon2);

    List<SiteDto> findSiteProjectionsByClientIds(List<Long> clientIds);

    List<SiteDto> findDtoByUserExternalId(UUID userExternalId);

    SiteDto findDtoById(Long siteId);

    Optional<SiteDto> findDtoByExternalId(boolean ignoreProjectFilter,
                                            List<Long> projectIds,
                                            UUID externalId);

    List<SiteDto> findDtosByProjectExternalId(boolean ignoreProjectFilter,
                                            List<Long> projectIds,
                                            UUID projectExternalId);

}
