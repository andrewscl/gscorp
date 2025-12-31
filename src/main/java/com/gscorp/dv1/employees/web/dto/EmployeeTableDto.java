package com.gscorp.dv1.employees.web.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.gscorp.dv1.employees.infrastructure.EmployeeTableProjection;

public record EmployeeTableDto (

    Long id,
    String photoUrl,
    String name,
    String fatherSurname,
    String motherSurname,
    String rut,
    String mail,
    String phone,
    String positionName,
    Boolean active,
    LocalDate hireDate,
    LocalDateTime createdAt,
    String username,
    String userMail,
    String userPhone,
    String fullName

){

    public static EmployeeTableDto fromProjection(EmployeeTableProjection p) {
        if (p == null) return null;

        String fullName = p.getFullName();

        return new EmployeeTableDto(
            p.getId(),
            p.getPhotoUrl(),
            p.getName(),
            p.getFatherSurname(),
            p.getMotherSurname(),
            p.getRut(),
            p.getMail(),
            p.getPhone(),
            p.getPositionName(),
            p.getActive(),
            p.getHireDate(),
            p.getCreatedAt(),
            p.getUsername(),
            p.getUserMail(),
            p.getUserPhone(),
            fullName
        );

    }

}
