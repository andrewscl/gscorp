package com.gscorp.dv1.attendance.web.dto.statistics;

public record AttendancesHourlyCountDto (
    String hour, 
    Long count
){}
