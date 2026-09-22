package com.gscorp.dv1.operations.shiftrequests.web.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.gscorp.dv1.enums.ShiftRequestType;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateShiftRequest(

    @NotNull(message = "siteId es obligatorio")
    UUID siteExternalId,

    UUID siteZoneExternalId,

    @NotNull(message = "type es obligatorio")
    ShiftRequestType type,

    Long accountId,

    @NotNull(message = "startDate es obligatorio")
    LocalDate startDate,
    LocalDate endDate,

    String description,
    
    @Valid @NotNull(message = "schedules es obligatorio")
    @Size(min = 1, message = "Debe ingresar al menos un tramo")
    List<ShiftScheduleRequest> schedules

) {}