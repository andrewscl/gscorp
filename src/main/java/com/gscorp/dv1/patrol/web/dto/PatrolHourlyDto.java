package com.gscorp.dv1.patrol.web.dto;

public record PatrolHourlyDto (

    Long patrolId,
    String patrolName,
    String hour,
    long count

){}
