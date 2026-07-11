package com.gscorp.dv1.hr.employees.web.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import com.gscorp.dv1.enums.EmployeeStatus;
import com.gscorp.dv1.enums.UserStatus;
import com.gscorp.dv1.hr.employees.infrastructure.Projections.EmployeeTableProjection;

public record EmployeeTableDto (

    Long id,
    UUID externalId,
    String photoUrl,
    String name,
    String fatherSurname,
    String motherSurname,
    String rut,
    String mail,
    String phone,
    String positionName,
    EmployeeStatus status,
    LocalDate hireDate,
    LocalDateTime createdAt,
    String username,
    String userMail,
    String userPhone,
    UserStatus userStatus,
    String fullName

){

    public static EmployeeTableDto fromProjection(EmployeeTableProjection p) {
        if (p == null) return null;

        String fullName = p.getFullName();

        return new EmployeeTableDto(
            p.getId(),
            p.getExternalId(),
            p.getPhotoUrl(),
            p.getName(),
            p.getFatherSurname(),
            p.getMotherSurname(),
            p.getRut(),
            p.getMail(),
            p.getPhone(),
            p.getPositionName(),
            p.getStatus(),
            p.getHireDate(),
            p.getCreatedAt(),
            p.getUsername(),
            p.getUserMail(),
            p.getUserPhone(),
            p.getUserStatus(),
            fullName
        );

    }

}
