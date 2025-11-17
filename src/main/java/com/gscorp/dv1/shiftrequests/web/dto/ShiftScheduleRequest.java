package com.gscorp.dv1.shiftrequests.web.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record ShiftScheduleRequest(
    @NotNull @NotEmpty(message = "dayFrom es obligatorio") String dayFrom,
    @NotNull @NotEmpty(message = "dayTo es obligatorio") String dayTo,
    @NotNull @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "startTime debe tener formato HH:mm") String startTime,
    @NotNull @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "endTime debe tener formato HH:mm") String endTime
) {}