package com.gscorp.dv1.shiftrequests.web.dto;

public record ShiftScheduleRequest(
    String dayFrom,
    String dayTo,
    String startTime,
    String endTime,
    String lunchTime
) {}