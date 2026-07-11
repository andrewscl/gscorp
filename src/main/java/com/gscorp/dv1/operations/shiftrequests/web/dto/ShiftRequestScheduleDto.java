package com.gscorp.dv1.operations.shiftrequests.web.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import com.gscorp.dv1.enums.DayOfWeek;
import com.gscorp.dv1.operations.shiftrequests.infrastructure.ShiftRequestSchedule;
import com.gscorp.dv1.operations.shiftrequests.infrastructure.projections.ShiftRequestScheduleProjection;

public record ShiftRequestScheduleDto (
    DayOfWeek dayFrom,
    DayOfWeek dayTo,
    LocalTime startTime,
    LocalTime endTime,
    LocalTime lunchTime,
    LocalDate requestStartDate,
    LocalDate requestEndDate
){

    public static ShiftRequestScheduleDto fromEntity(ShiftRequestSchedule s) {
        if (s == null) return null;
        return new ShiftRequestScheduleDto(
            s.getDayFrom(),
            s.getDayTo(),
            s.getStartTime(),
            s.getEndTime(),
            s.getLunchTime(),
            s.getShiftRequest().getStartDate(),
            s.getShiftRequest().getEndDate()
        );
    }

    public static ShiftRequestScheduleDto fromProjection(ShiftRequestScheduleProjection sp) {
        if (sp == null) return null;
        return new ShiftRequestScheduleDto(
            sp.getDayFrom(),
            sp.getDayTo(),
            sp.getStartTime(),
            sp.getEndTime(),
            sp.getLunchTime(),
            sp.getRequestStartDate(),
            sp.getRequestEndDate()
        );
    }
    
}
