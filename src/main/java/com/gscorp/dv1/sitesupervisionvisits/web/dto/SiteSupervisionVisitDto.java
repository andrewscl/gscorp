package com.gscorp.dv1.sitesupervisionvisits.web.dto;

import java.time.OffsetDateTime;

import com.gscorp.dv1.employees.infrastructure.Employee;
import com.gscorp.dv1.sites.infrastructure.Site;
import com.gscorp.dv1.sitesupervisionvisits.infrastructure.SiteVisit;

public record SiteSupervisionVisitDto(
    Long id,
    Long employeeId,
    String employeeName,
    Long siteId,
    String siteName,
    OffsetDateTime visitDateTime,
    Double latitude,
    Double longitude,
    String description,
    String photoPath,
    String videoPath,
    String visitDateTimeFormatted
) {
    public static SiteSupervisionVisitDto fromEntity(SiteVisit visit, String visitDateTimeFormatted) {
        if (visit == null) return null;
            Employee emp = visit.getEmployee();
            Site site = visit.getSite();

        return new SiteSupervisionVisitDto(
            visit.getId(),
            emp != null ? emp.getId() : null,
            emp != null ? emp.getName() : null,
            site != null ? site.getId() : null,
            site != null ? site.getName() : null,
            visit.getVisitDateTime(),
            visit.getLatitude(),
            visit.getLongitude(),
            visit.getDescription(),
            visit.getPhotoPath(),
            visit.getVideoPath(),
            visitDateTimeFormatted
        );
    }

    // versión por compatibilidad que no recibe formatted
    public static SiteSupervisionVisitDto fromEntity(SiteVisit visit) {
        return fromEntity(visit, null);
    }

}
