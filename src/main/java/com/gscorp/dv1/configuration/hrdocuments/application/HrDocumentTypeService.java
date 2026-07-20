package com.gscorp.dv1.configuration.hrdocuments.application;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.gscorp.dv1.config.security.SecurityUser;
import com.gscorp.dv1.configuration.hrdocuments.web.dto.CreateHrDocumentType;
import com.gscorp.dv1.configuration.hrdocuments.web.dto.HrDocumentTypeDto;
import com.gscorp.dv1.enums.EmployeeStatus;
import com.gscorp.dv1.enums.HrProcessType;

public interface HrDocumentTypeService {
    Page<HrDocumentTypeDto> findByStatusAndProcess (
                                EmployeeStatus status,
                                HrProcessType process,
                                Pageable pageable);

    HrDocumentTypeDto createHrDocumentType(
                        CreateHrDocumentType request,
                        SecurityUser securityUser);

}
