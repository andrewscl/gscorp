package com.gscorp.dv1.employees.web.dto;

import com.gscorp.dv1.employees.infrastructure.EmployeeSelectProjection;

public record EmployeeSelectDto (
    Long id,
    String name,
    String fatherSurname,
    String motherSurname,
    String fullName
){
    public static EmployeeSelectDto fromProjection(EmployeeSelectProjection esp) {
        if (esp == null) return null;

        // Calcular fullName aquí mismo
        String fullName = String.join(" ",
            esp.getName() != null ? esp.getName() : "",
            esp.getFatherSurname() != null ? esp.getFatherSurname() : "",
            esp.getMotherSurname() != null ? esp.getMotherSurname() : ""
        ).trim();

        return new EmployeeSelectDto(
            esp.getId(),
            esp.getName(),
            esp.getFatherSurname(),
            esp.getMotherSurname(),
            fullName
        );
    }
}
