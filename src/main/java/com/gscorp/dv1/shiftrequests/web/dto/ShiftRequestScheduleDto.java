package com.gscorp.dv1.shiftrequests.web.dto;

import com.gscorp.dv1.shiftrequests.infrastructure.ShiftRequestSchedule;

public record ShiftRequestScheduleDto (
    String dayFrom,
    String dayTo,
    String startTime,
    String endTime,
    String lunchTime
){
    public static ShiftRequestScheduleDto fromEntity(ShiftRequestSchedule s) {
        if (s == null) return null;
        return new ShiftRequestScheduleDto(
            s.getDayFrom(),
            s.getDayTo(),
            s.getStartTime() != null ? s.getStartTime().toString() : null,
            s.getEndTime() != null ? s.getEndTime().toString() : null,
            s.getLunchTime() != null ? s.getLunchTime().toString() : null
        );
    }
    
}
