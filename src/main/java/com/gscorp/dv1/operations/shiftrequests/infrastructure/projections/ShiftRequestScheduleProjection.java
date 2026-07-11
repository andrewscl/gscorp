package com.gscorp.dv1.operations.shiftrequests.infrastructure.projections;

import java.time.LocalDate;
import java.time.LocalTime;

import com.gscorp.dv1.enums.DayOfWeek;

public interface ShiftRequestScheduleProjection {
    Long getId();
    Long getShiftRequestId();
    DayOfWeek getDayFrom();
    DayOfWeek getDayTo();
    LocalTime getStartTime();
    LocalTime getEndTime();
    LocalTime getLunchTime();
    LocalDate getRequestStartDate();
    LocalDate getRequestEndDate();
}
