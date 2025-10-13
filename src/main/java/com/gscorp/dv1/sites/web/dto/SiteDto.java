package com.gscorp.dv1.sites.web.dto;

public record SiteDto (
    Long id,
    Long projectId,
    String projectName,
    String name,
    String code,
    String address,
    String timeZone,
    Boolean active
) {}