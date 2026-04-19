package com.gscorp.dv1.patrol.web.dto;

import java.util.List;

public record CreatePatrolRequest (
    Long siteId,
    String name,
    String description,
    Integer dayFrom,
    Integer dayTo,
    List<String> scheduleTimes,
    List<String> checkpoints
){}
