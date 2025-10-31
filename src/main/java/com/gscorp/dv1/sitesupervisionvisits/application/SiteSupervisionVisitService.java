package com.gscorp.dv1.sitesupervisionvisits.application;

import com.gscorp.dv1.sitesupervisionvisits.web.dto.CreateSiteSupervisionVisitDto;
import com.gscorp.dv1.sitesupervisionvisits.web.dto.SiteSupervisionVisitDto;

public interface SiteSupervisionVisitService {

    SiteSupervisionVisitDto saveSiteSupervisionVisit(
                                CreateSiteSupervisionVisitDto dto);

}
