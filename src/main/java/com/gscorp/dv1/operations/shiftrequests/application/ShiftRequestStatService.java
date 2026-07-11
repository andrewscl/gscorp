package com.gscorp.dv1.operations.shiftrequests.application;

import java.util.List;
import java.util.UUID;

import com.gscorp.dv1.operations.shiftrequests.web.dto.statistics.ProjectSiteShiftRequestsSummaryDto;

public interface ShiftRequestStatService {

    List<ProjectSiteShiftRequestsSummaryDto>
                getProjectSiteShiftRequestsSummaryTodaySummaryByUserExternalId(
                                                            UUID userExternalId);



}
