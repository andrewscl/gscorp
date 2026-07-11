package com.gscorp.dv1.core.nationalities.web.dto;

public record CreateNationalityRequest(
    String name,
    String isoCode
) {}
