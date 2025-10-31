package com.gscorp.dv1.sitesupervisionvisits.web.dto;

import java.time.OffsetDateTime;

import com.gscorp.dv1.employees.infrastructure.Employee;
import com.gscorp.dv1.sites.infrastructure.Site;
import com.gscorp.dv1.sitesupervisionvisits.infrastructure.SiteSupervisionVisit;

public record SiteSupervisionVisitDto(
    Long id,
    Employee supervisor,
    Site site,
    OffsetDateTime visitDateTime,
    String description,
    String photoPath,
    String videoPath
) {
    public static SiteSupervisionVisitDto fromEntity(SiteSupervisionVisit visit) {
        if (visit == null) return null;
        return new SiteSupervisionVisitDto(
            visit.getId(),
            visit.getSupervisor(),
            visit.getSite(),
            visit.getVisitDateTime(),
            visit.getDescription(),
            visit.getPhotoPath(),
            visit.getVideoPath()
        );
    }
}
