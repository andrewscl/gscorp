package com.gscorp.dv1.operations.sitezones.application;

import java.util.List;
import java.util.UUID;

import com.gscorp.dv1.enums.SiteZoneStatus;
import com.gscorp.dv1.operations.sitezones.web.dto.SiteZoneDto;

public interface SiteZoneService {
    
    List<SiteZoneDto> getSiteZones(
                            UUID userExternalId,
                            UUID siteId,
                            SiteZoneStatus status);

}
