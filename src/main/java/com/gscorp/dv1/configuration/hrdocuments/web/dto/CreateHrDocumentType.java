package com.gscorp.dv1.configuration.hrdocuments.web.dto;

import com.gscorp.dv1.enums.EmployeeStatus;
import com.gscorp.dv1.enums.HrProcessType;

public record CreateHrDocumentType (
    String name,
    EmployeeStatus status,
    HrProcessType targetProcess
){
    
}
