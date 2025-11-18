package com.gscorp.dv1.sitesupervisionvisits.application;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;

import com.gscorp.dv1.sitesupervisionvisits.web.dto.CreateSiteSupervisionVisitRequest;
import com.gscorp.dv1.sitesupervisionvisits.web.dto.SiteSupervisionVisitDto;
import com.gscorp.dv1.sitesupervisionvisits.web.dto.SiteVisitCountDto;
import com.gscorp.dv1.sitesupervisionvisits.web.dto.SiteVisitHourlyDto;

public interface SiteSupervisionVisitService {

    SiteSupervisionVisitDto createSiteSupervisionVisitRequest(
                                CreateSiteSupervisionVisitRequest req);

    List<SiteSupervisionVisitDto> getAllSiteSupervisionVisits();

    SiteSupervisionVisitDto findByIdWithEmployeeAndSite(Long id);

    long countByClientIdAndDate(Long clientId, LocalDate date);

    List<SiteSupervisionVisitDto> findByClientIdAndDateBetween
            (Long clientId, OffsetDateTime from, OffsetDateTime to);

    List<SiteVisitCountDto> getVisitsBySite
            (Long clientId, OffsetDateTime from, OffsetDateTime to);

    List<SiteVisitHourlyDto> getSiteVisitHourlyCounts
            (Collection<Long> clientIds, LocalDate date, String tz);

    /**
     * Cuenta visitas por clientes en la fecha indicada. tz puede ser null para usar ZoneId.systemDefault().
     */
    long countByClientIdsAndDate(List<Long> clientIds, LocalDate date, String tz);

    /**
     * Conveniencia: usa tz por defecto (system zone)
     */
    default long countByClientIdsAndDate(List<Long> clientIds, LocalDate date) {
        return countByClientIdsAndDate(clientIds, date, null);
    }

    long countByClientIdAndDate(Long clientId, LocalDate date, String tz);

}
