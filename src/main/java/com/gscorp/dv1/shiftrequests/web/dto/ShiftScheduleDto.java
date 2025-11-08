package com.gscorp.dv1.shiftrequests.web.dto;

import com.gscorp.dv1.shiftrequests.infrastructure.ShiftSchedule;

public record ShiftScheduleDto (
    String dayFrom,
    String dayTo,
    String startTime,
    String endTime,
    String lunchTime
){
    public static ShiftScheduleDto fromEntity(ShiftSchedule s) {
        if (s == null) return null;
        return new ShiftScheduleDto(
            s.getDayFrom(),
            s.getDayTo(),
            s.getStartTime() != null ? s.getStartTime().toString() : null,
            s.getEndTime() != null ? s.getEndTime().toString() : null,
            s.getLunchTime() != null ? s.getLunchTime().toString() : null
        );
    }
    
}
