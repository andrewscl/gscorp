package com.gscorp.dv1.sitesupervisionvisits.application;

import java.time.LocalDate;
import java.util.List;

import com.gscorp.dv1.sitesupervisionvisits.web.dto.CreateSiteSupervisionVisitRequest;
import com.gscorp.dv1.sitesupervisionvisits.web.dto.SiteSupervisionVisitDto;

public interface SiteSupervisionVisitService {

    SiteSupervisionVisitDto createSiteSupervisionVisitRequest(
                                CreateSiteSupervisionVisitRequest req);

    List<SiteSupervisionVisitDto> getAllSiteSupervisionVisits();

    SiteSupervisionVisitDto findByIdWithEmployeeAndSite(Long id);

    List<SiteSupervisionVisitDto> findByClientIdAndDateBetween(Long clientId, LocalDate fromDate, LocalDate toDate);

    long countByClientIdAndDate(Long clientId, LocalDate date);


}
