package com.gscorp.dv1.operations.shifts.application;

import java.util.List;
import java.util.UUID;

import com.gscorp.dv1.operations.shifts.web.dto.statistics.ProjectSiteShiftsSummaryDto;

public interface ShiftStatService {

    List<ProjectSiteShiftsSummaryDto> getLast24hoursProjectSiteShiftsSummary (
                                                    UUID userExternalId,
                                                    String zoneIdStr);

}
