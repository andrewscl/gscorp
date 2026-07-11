package com.gscorp.dv1.operations.sitevisits.application;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import com.gscorp.dv1.operations.sitevisits.web.dto.CreateSiteSupervisionVisit;
import com.gscorp.dv1.operations.sitevisits.web.dto.SiteVisitCountDto;
import com.gscorp.dv1.operations.sitevisits.web.dto.SiteVisitDto;
import com.gscorp.dv1.operations.sitevisits.web.dto.SiteVisitHourlyDto;
import com.gscorp.dv1.operations.sitevisits.web.dto.SiteVisitPointDto;

public interface SiteVisitService {

    SiteVisitDto createSiteSupervisionVisitRequest(
                                CreateSiteSupervisionVisit req,
                                UUID userExternalId);

    List<SiteVisitDto> findByUserAndDateBetween(
                                UUID userExternalId,
                                LocalDate fromDate,
                                LocalDate toDate,
                                String clientTz);

    SiteVisitDto findByIdWithEmployeeAndSite(Long id);

    long countByClientIdAndDate(Long clientId, LocalDate date);

    List<SiteVisitCountDto> getVisitsBySite
            (Long clientId, OffsetDateTime from, OffsetDateTime to);

    /**
     * Obtiene las visitas por horra agregadas para un usuario en una fecha dada.
     * tz puede ser null para usar ZoneId.systemDefault().
     */
    List<SiteVisitHourlyDto> getVisitsSeriesForUserByDateByVisitHourlyAgregated
            (UUID userExternalId, LocalDate date, String tz);

    /**
     * Cuenta visitas por clientes en la fecha indicada.
     * tz puede ser null para usar ZoneId.systemDefault().
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
                (UUID userExternalId, LocalDate fromDate, LocalDate toDate, ZoneId zone);

}
