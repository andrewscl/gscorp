package com.gscorp.dv1.hr.employeetransitionrequests.application;

import java.util.UUID;

import org.springframework.data.domain.Page;

import com.gscorp.dv1.enums.EmployeeTransitionRequestStatus;
import com.gscorp.dv1.hr.employeetransitionrequests.web.dto.EmployeeTransitionRequestDto;

public interface EmployeeTransitionRequestService {

    Page<EmployeeTransitionRequestDto> getTransitionRequestTable(
                UUID userExternalId,
                EmployeeTransitionRequestStatus status,
                int page,
                int size);

}
