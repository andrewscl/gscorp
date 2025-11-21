package com.gscorp.dv1.forecast.infrastructure;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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

    /**
     * Variante por intersección: devuelve forecasts cuya ventana [periodStart, periodEnd]
     * intersecta el intervalo pedido. Semántica: periodStart <= toDate AND periodEnd >= fromDate.
     */
    @Query("""
        SELECT DISTINCT f
        FROM Forecast f
        JOIN FETCH f.site s
        JOIN s.project p
        WHERE p.client.id IN :clientIds
          AND f.isActive = true
          AND f.periodStart <= :toDate
          AND f.periodEnd   >= :fromDate
    """)
    List<Forecast> findByClientIdsAndDateRangeIntersect(
            @Param("clientIds") List<Long> clientIds,
            @Param("fromDate") OffsetDateTime fromDate,
            @Param("toDate") OffsetDateTime toDate
    );

    /*
     * Alternativa más simple si prefieres filtrar por la columna client_id (si guardas clientId Long en Forecast):
     *
     * List<Forecast> findByClientIdInAndPeriodStartBetweenAndIsActiveTrue(
     *      List<Long> clientIds, OffsetDateTime from, OffsetDateTime to);
     *
     * O, si tu entidad mantiene LocalDate para periodStart, usa LocalDate params.
     */

        @Query("""
        select new com.gscorp.dv1.forecast.web.dto.ForecastTableRowDto(
                f.id,
                f.projectId,
                p.name,
                f.siteId,
                s.name,
                f.metric,
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
        and f.periodStart between :from and :to
        order by f.periodStart desc
        """)
        List<ForecastTableRowDto> findRowsForClientIdsAndDates(
        @Param("clientIds") List<Long> clientIds,
        @Param("from") OffsetDateTime from,
        @Param("to") OffsetDateTime to
);

}
