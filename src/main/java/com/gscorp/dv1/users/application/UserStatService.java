package com.gscorp.dv1.users.application;

import java.util.List;

import com.gscorp.dv1.users.web.dto.statistics.UserStatusSummaryDto;

public interface UserStatService {

    List<UserStatusSummaryDto> getUsersStatusSummary();

}
