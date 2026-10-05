package com.gscorp.dv1.operations.sitezones.application;

import java.util.List;
import java.util.UUID;

import com.gscorp.dv1.enums.SiteZoneStatus;
import com.gscorp.dv1.operations.sitezones.infrastructure.SiteZone;
import com.gscorp.dv1.operations.sitezones.web.dto.CreateSiteZoneRequest;
import com.gscorp.dv1.operations.sitezones.web.dto.SiteZoneDto;

public interface SiteZoneService {
    
    List<SiteZoneDto> getSiteZones(
                            UUID userExternalId,
                            UUID siteExternalId,
                            SiteZoneStatus status);

    SiteZoneDto createSiteZone (
                        UUID userExternalId,
                        CreateSiteZoneRequest request);

    void delete (UUID userExternalId,
                    UUID siteZoneExternalId);

    SiteZone findByExternalId(UUID siteZoneExternalId);

}
