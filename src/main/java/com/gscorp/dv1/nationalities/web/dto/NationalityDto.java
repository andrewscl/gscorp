package com.gscorp.dv1.nationalities.web.dto;

import com.gscorp.dv1.nationalities.infrastructure.Nationality;

public record NationalityDto(
    Long id,
    String name,
    String isoCode
) {
    public static NationalityDto fromEntity(Nationality n) {
        if (n == null) return null;
        return new NationalityDto(
            n.getId(),
            n.getName(),
            n.getIsoCode()
        );
    }
}
