package com.gscorp.dv1.nationalities.web.dto;

public record CreateNationalityRequest(
    String name,
    String isoCode
) {}
