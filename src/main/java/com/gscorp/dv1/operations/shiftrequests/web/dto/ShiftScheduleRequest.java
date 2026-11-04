package com.gscorp.dv1.operations.shiftrequests.web.dto;

import com.gscorp.dv1.enums.DayOfWeek;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record ShiftScheduleRequest(
    @NotNull (message = "dayFrom es obligatorio")
    DayOfWeek dayFrom,

    @NotNull (message = "dayTo es obligatorio")
    DayOfWeek dayTo,

    @NotNull @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "startTime debe tener formato HH:mm")
    String startTime,

    @NotNull @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "endTime debe tener formato HH:mm")
    String endTime

) {}