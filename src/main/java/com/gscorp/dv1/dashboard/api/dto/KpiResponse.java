package com.gscorp.dv1.dashboard.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDate;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record KpiResponse(
    Long clientId,
    LocalDate from,
    LocalDate to,
    AttendanceKpi attendance,
    PatrolKpi patrol,
    IncidentKpi incidents
){
  public record AttendanceKpi(
      Double rate,          // 0..1
      Long   entries,       // marcaciones IN reales
      Long   expected,      // dotación estimada (o planificada) * días
      Double delta,         // diferencia de rate vs periodo anterior (puntos, e.g. +0.07)
      Double deltaPct       // variación relativa, e.g. +12.5 (%)
  ){}

  public record PatrolKpi(
      Double compliance,    // 0..1
      Integer completedHits,
      Integer expectedHits,
      Double delta,
      Double deltaPct
  ){}

  public record IncidentKpi(
      Long total,
      Long critical,        // opcional, si clasificas
      Long open,            // opcional, si manejas estado
      Long closed,          // opcional
      Long meanPerDay       // opcional
  ){}
}
