package com.gscorp.dv1.employees.web.dto;

import com.gscorp.dv1.employees.infrastructure.EmployeeSelectProjection;

public record EmployeeSelectDto (
    Long id,
    String name,
    String fatherSurname,
    String motherSurname
){
    public static EmployeeSelectDto fromProjection(EmployeeSelectProjection esp) {
        if (esp == null) return null;

        return new EmployeeSelectDto(
            esp.getId(),
            esp.getName(),
            esp.getFatherSurname(),
            esp.getMotherSurname()
        );
    }
}
