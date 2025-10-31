package com.gscorp.dv1.sitesupervisionvisits.web.dto;

import com.gscorp.dv1.employees.infrastructure.Employee;

public record CreateSiteSupervisionVisitDto (
    Employee supervisor,
    Long siteId,
    String description,
    String photoPath,
    String videoPath
){
    
}
