package com.gscorp.dv1.forecast.application;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import com.gscorp.dv1.forecast.web.dto.ForecastFormPayload;
import com.gscorp.dv1.forecast.web.dto.ForecastPointDto;
import com.gscorp.dv1.forecast.web.dto.ForecastTableRowDto;

public interface ForecastService {
    

    List<ForecastPointDto> getForecastSeriesForUserByDates
                (Long userId, LocalDate fromDate, LocalDate toDate, ZoneId zone);

    /**
     * Devuelve la lista de ForecastRecordDto para la vista de tabla (detalle),
     * filtrada por los clients a los que el usuario tiene acceso y por el rango de fechas.
     *
     * fromDate/toDate se interpretan en la zona indicada por zone.
     */
    List<ForecastTableRowDto> loadTableRowForUserAndDates(Long userId, LocalDate fromDate, LocalDate toDate, ZoneId zone);

    ForecastFormPayload prepareCreateForecastForm(Long userId);


}
