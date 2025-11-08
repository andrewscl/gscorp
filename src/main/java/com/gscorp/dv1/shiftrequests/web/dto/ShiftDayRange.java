package com.gscorp.dv1.shiftrequests.web.dto;

import java.time.LocalTime;

public record ShiftDayRange (
    String DayFrom,
    String DayTo,
    LocalTime StartTime,
    LocalTime EndTime,
    LocalTime LunchTime
){}
