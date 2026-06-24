package com.gscorp.dv1.shiftrequests.application;

import java.util.List;
import java.util.UUID;

import com.gscorp.dv1.shiftrequests.web.dto.statistics.ProjectSiteShiftRequestsSummaryDto;

public interface ShiftRequestStatService {

    List<ProjectSiteShiftRequestsSummaryDto>
                getProjectSiteShiftRequestsSummaryTodaySummaryByUserExternalId(
                                                                UUID userExternalId);

}
