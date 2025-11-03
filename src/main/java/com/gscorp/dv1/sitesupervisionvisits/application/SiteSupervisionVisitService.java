package com.gscorp.dv1.sitesupervisionvisits.application;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import com.gscorp.dv1.sitesupervisionvisits.web.dto.CreateSiteSupervisionVisitRequest;
import com.gscorp.dv1.sitesupervisionvisits.web.dto.SiteSupervisionVisitDto;
import com.gscorp.dv1.sitesupervisionvisits.web.dto.SiteVisitCountDto;

public interface SiteSupervisionVisitService {

    SiteSupervisionVisitDto createSiteSupervisionVisitRequest(
                                CreateSiteSupervisionVisitRequest req);

    List<SiteSupervisionVisitDto> getAllSiteSupervisionVisits();

    SiteSupervisionVisitDto findByIdWithEmployeeAndSite(Long id);

    long countByClientIdAndDate(Long clientId, LocalDate date);

    List<SiteSupervisionVisitDto> findByClientIdAndDateBetween(Long clientId, OffsetDateTime from, OffsetDateTime to);

    List<SiteVisitCountDto> getVisitsBySite(Long clientId, OffsetDateTime from, OffsetDateTime to);
}
