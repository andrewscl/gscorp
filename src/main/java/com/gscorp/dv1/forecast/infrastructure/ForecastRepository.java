package com.gscorp.dv1.forecast.infrastructure;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.gscorp.dv1.enums.ForecastMetric;
import com.gscorp.dv1.forecast.web.dto.ForecastTableRowDto;

@Repository
public interface ForecastRepository extends JpaRepository<Forecast, Long>{


    /**
     * Busca Forecasts asociados a clientes (a través de site->project->client)
     * cuyo periodStart está dentro del rango inclusive [fromDate .. toDate].
     * Usamos DISTINCT para evitar duplicados por JOIN FETCH.
     *
     * Nota: params son OffsetDateTime porque la entidad usa OffsetDateTime.
     */
    @Query("""
        SELECT DISTINCT f
        FROM Forecast f
        JOIN FETCH f.site s
        JOIN s.project p
        WHERE p.client.id IN :clientIds
          AND f.periodStart >= :fromDate
          AND f.periodStart <= :toDate
          AND f.isActive = true
    """)
    List<Forecast> findByClientIdsAndDateBetween(
            @Param("clientIds") List<Long> clientIds,
            @Param("fromDate") OffsetDateTime fromDate,
            @Param("toDate") OffsetDateTime toDate
    );


    @Query("""
        SELECT f.periodStart AS periodStart,
               f.periodEnd   AS periodEnd,
               f.value       AS value,
               f.periodicity AS periodicity,
               f.forecastMetric AS metric,
               f.siteId      AS siteId,
               s.projectId   AS projectId
        FROM Forecast f
        WHERE f.clientId IN :clientIds
          AND f.isActive = true
          AND f.forecastMetric = :metric
          AND f.periodStart <= :toDate
          AND f.periodEnd   >= :fromDate
          AND (:fsiteId IS NULL OR f.siteId = :fsiteId)
          AND (:fprojectId IS NULL OR f.projectId = :fprojectId)
        ORDER BY f.periodStart
    """)
    List<ForecastSeriesProjection> findProjectionByClientIdsAndDateRangeIntersectAndMetric(
        @Param("clientIds") Collection<Long> clientIds,
        @Param("fromDate") OffsetDateTime fromDate,
        @Param("toDate") OffsetDateTime toDate,
        @Param("metric") ForecastMetric metric,
        @Param("fsiteId") Long fsiteId,
        @Param("fprojectId") Long fprojectId
    );


    @Query("""
        select new com.gscorp.dv1.forecast.web.dto.ForecastTableRowDto(
                f.id,
                f.projectId,
                p.name,
                f.siteId,
                s.name,
                f.forecastMetric,
                f.periodicity,
                f.periodStart,
                f.periodEnd,
                f.value,
                f.units,
                f.isActive
        )
        from Forecast f
        left join f.project p
        left join f.site s
        where f.clientId in :clientIds
          and (:siteName IS NULL OR :siteName = '' OR LOWER(s.name) LIKE LOWER(CONCAT('%', :siteName, '%')))
          and (:metric IS NULL OR f.forecastMetric = :metric)
        order by f.periodStart desc
        """)
    List<ForecastTableRowDto> findRowsForClientIdsAndFilters(
        @Param("clientIds") List<Long> clientIds,
        @Param("siteName") String siteName,
        @Param("metric") ForecastMetric metric
    );


}
