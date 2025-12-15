package com.gscorp.dv1.shiftrequests.infrastructure;

import java.time.LocalDate;
import java.time.LocalTime;

public interface ShiftRequestScheduleProjection {
    Long getId();
    Long getShiftRequestId();
    String getDayFrom();
    String getDayTo();
    LocalTime getStartTime();
    LocalTime getEndTime();

    // new: vigencia del ShiftRequest que contiene este schedule
    LocalDate getRequestStartDate();
    LocalDate getRequestEndDate();
}
