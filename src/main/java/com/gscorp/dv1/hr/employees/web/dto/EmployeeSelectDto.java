package com.gscorp.dv1.hr.employees.web.dto;

import java.util.UUID;

import com.gscorp.dv1.hr.employees.infrastructure.Projections.EmployeeSelectProjection;

public record EmployeeSelectDto (
    Long id,
    UUID externalId,
    String name,
    String fatherSurname,
    String motherSurname,
    String fullName,
    Long userId
){
    public static EmployeeSelectDto fromProjection(EmployeeSelectProjection esp) {
        if (esp == null) return null;

        String fullName = String.join(" ",
            esp.getName() != null ? esp.getName() : "",
            esp.getFatherSurname() != null ? esp.getFatherSurname() : "",
            esp.getMotherSurname() != null ? esp.getMotherSurname() : ""
        ).trim();

        return new EmployeeSelectDto(
            esp.getId(),
            esp.getExternalId(),
            esp.getName(),
            esp.getFatherSurname(),
            esp.getMotherSurname(),
            fullName,
            esp.getUserId()
        );
    }
}
