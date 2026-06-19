package com.gscorp.dv1.admin.web.dto;

import java.util.List;

import com.gscorp.dv1.users.web.dto.statistics.CompanyUsersStatusSummaryDto;
import com.gscorp.dv1.users.web.dto.statistics.RoleUsersSummaryDto;
import com.gscorp.dv1.users.web.dto.statistics.UserStatusSummaryDto;

public record AdminDistributionMetricResponse (
    List<UserStatusSummaryDto> usersStatusSummary,
    List<CompanyUsersStatusSummaryDto> companyUsersSummary,
    List<RoleUsersSummaryDto> roleUsersSummary
){}
