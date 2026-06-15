package com.gscorp.dv1.forecast.application;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import org.springframework.security.core.Authentication;

import com.gscorp.dv1.enums.ForecastMetric;
import com.gscorp.dv1.forecast.web.dto.ForecastCreateDto;
import com.gscorp.dv1.forecast.web.dto.ForecastFormPayload;
import com.gscorp.dv1.forecast.web.dto.ForecastPointDto;
import com.gscorp.dv1.forecast.web.dto.ForecastRecordDto;
import com.gscorp.dv1.forecast.web.dto.ForecastTableRowDto;

public interface ForecastService {


    List<ForecastPointDto> getForecastSeriesForUserByDates(
                UUID externalId,
                LocalDate fromDate,
                LocalDate toDate,
                ZoneId zone,
                ForecastMetric metric,
                Long siteId,
                Long projectId);

    List<ForecastTableRowDto> findRowsFilteredForUser(UUID externalId, String siteName, String metric, ZoneId zone);


    ForecastFormPayload prepareCreateForecastForm(UUID externalId);


    ForecastRecordDto createForecast (
                            ForecastCreateDto req,
                            UUID externalId,
                            Authentication authentication);


}