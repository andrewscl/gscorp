package com.gscorp.dv1.sitesupervisionvisits.application;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

import com.gscorp.dv1.sitesupervisionvisits.web.dto.CreateSiteSupervisionVisitRequest;
import com.gscorp.dv1.sitesupervisionvisits.web.dto.SiteVisitPointDto;
import com.gscorp.dv1.sitesupervisionvisits.web.dto.SiteSupervisionVisitDto;
import com.gscorp.dv1.sitesupervisionvisits.web.dto.SiteVisitCountDto;
import com.gscorp.dv1.sitesupervisionvisits.web.dto.SiteVisitHourlyDto;

public interface SiteVisitService {

    SiteSupervisionVisitDto createSiteSupervisionVisitRequest(
                                CreateSiteSupervisionVisitRequest req, Long userId);

    List<SiteSupervisionVisitDto> getAllSiteSupervisionVisits();

    SiteSupervisionVisitDto findByIdWithEmployeeAndSite(Long id);

    long countByClientIdAndDate(Long clientId, LocalDate date);

    List<SiteSupervisionVisitDto> findByClientIdAndDateBetween
            (Long clientId, OffsetDateTime from, OffsetDateTime to);

    List<SiteVisitCountDto> getVisitsBySite
            (Long clientId, OffsetDateTime from, OffsetDateTime to);

    /**
     * Obtiene las visitas por horra agregadas para un usuario en una fecha dada.
     * tz puede ser null para usar ZoneId.systemDefault().
     */
    List<SiteVisitHourlyDto> getVisitsSeriesForUserByDateByVisitHourlyAgregated
            (Long userId, LocalDate date, String tz);

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

    List<SiteVisitPointDto> getVisitsSeriesForUserByDates
                (Long userId, LocalDate fromDate, LocalDate toDate, ZoneId zone);

}
