package com.gscorp.dv1.forecast.application;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import com.gscorp.dv1.enums.ForecastMetric;
import com.gscorp.dv1.forecast.web.dto.ForecastCreateDto;
import com.gscorp.dv1.forecast.web.dto.ForecastFormPayload;
import com.gscorp.dv1.forecast.web.dto.ForecastPointDto;
import com.gscorp.dv1.forecast.web.dto.ForecastRecordDto;
import com.gscorp.dv1.forecast.web.dto.ForecastTableRowDto;

public interface ForecastService {


    List<ForecastPointDto> getForecastSeriesForUserByDates(
                Long userId,
                LocalDate fromDate,
                LocalDate toDate,
                ZoneId zone,
                ForecastMetric metric,
                Long siteId,
                Long projectId);

    List<ForecastTableRowDto> findRowsFilteredForUser(Long userId, String siteName, String metric, ZoneId zone);


    ForecastFormPayload prepareCreateForecastForm(Long userId);


    ForecastRecordDto createForecast (ForecastCreateDto req, Long userId);


}