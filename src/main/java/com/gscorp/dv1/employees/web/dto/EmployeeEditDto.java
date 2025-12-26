package com.gscorp.dv1.employees.web.dto;

import java.time.LocalDate;

import com.gscorp.dv1.employees.infrastructure.EmployeeEditProjection;
import com.gscorp.dv1.enums.Gender;
import com.gscorp.dv1.enums.MaritalStatus;
import com.gscorp.dv1.enums.StudyLevel;

public record EmployeeEditDto (

    Long id,
    String name,
    String fatherSurname,
    String motherSurname,
    String rut,
    String mail,
    String phone,
    String secondaryPhone,
    LocalDate hireDate,
    LocalDate birthDate,
    LocalDate exitDate,
    String address,
    Boolean active,
    Gender gender,
    Long nationalityId,
    MaritalStatus maritalStatus,
    StudyLevel studyLevel,
    Long professionId

) {
    public static EmployeeEditDto fromProjection(EmployeeEditProjection p) {
        return new EmployeeEditDto(
            p.getId(),
            p.getName(),
            p.getFatherSurname(),
            p.getMotherSurname(),
            p.getRut(),
            p.getMail(),
            p.getPhone(),
            p.getSecondaryPhone(),
            p.getHireDate(),
            p.getBirthDate(),
            p.getExitDate(),
            p.getAddress(),
            p.getActive(),
            p.getGender(),
            p.getNationalityId(),
            p.getMaritalStatus(),
            p.getStudyLevel(),
            p.getProfessionId()

        );
    }
}
