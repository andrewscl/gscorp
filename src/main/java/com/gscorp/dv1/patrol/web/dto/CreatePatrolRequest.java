package com.gscorp.dv1.patrol.web.dto;

public record CreatePatrolRequest (
    Long siteId,
    String name,
    String description,
    Integer dayFrom,
    Integer dayTo,
    String startTime,
    String tz
){}
