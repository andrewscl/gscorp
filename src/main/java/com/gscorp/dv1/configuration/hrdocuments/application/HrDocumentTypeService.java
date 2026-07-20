package com.gscorp.dv1.configuration.hrdocuments.application;

import java.util.UUID;

import org.springframework.data.domain.Page;

import com.gscorp.dv1.configuration.hrdocuments.web.dto.HrDocumentTypeDto;
import com.gscorp.dv1.enums.EmployeeStatus;
import com.gscorp.dv1.enums.HrProcessType;

public interface HrDocumentTypeService {
    Page<HrDocumentTypeDto> findByStatusAndProcess (
                            EmployeeStatus status, HrProcessType process);

    Page<HrDocumentTypeDto> getHrDocumentTypesList(
                        UUID userExternalId,
                        EmployeeStatus status,
                        HrProcessType targetProcess,
                        int page,
                        int size);


}
