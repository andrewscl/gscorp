package com.gscorp.dv1.hr.hrdocs.application;

import java.util.List;
import java.util.UUID;

import com.gscorp.dv1.hr.hrdocs.web.dto.HumanResourcesDocumentDto;

public interface HumanResourcesDocumentService {
    
    List<HumanResourcesDocumentDto> findByEmployeeTerminationExternalId(
                                                    UUID employeeTerminationExternalId);

}
