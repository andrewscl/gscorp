package com.gscorp.dv1.employees.web.dto;

import java.time.LocalDate;

import com.gscorp.dv1.employees.infrastructure.EmployeeViewProjection;

public record EmployeeViewDto (

    Long id,
    String name,
    String fatherSurname,
    String motherSurname,
    String photoUrl,
    String rut,
    String mail,
    String phone,
    String secondaryPhone,
    LocalDate hireDate,
    LocalDate birthDate,
    LocalDate exitDate,
    String address,
    Boolean active,
    String gender,
    String nationality,
    String maritalStatus,
    String studyLevel,
    String previtionalSystem,
    String pensionEntity,
    String healthSystem,
    String healthEntity,
    String paymentMethod,
    String bank,
    String bankAccountType,
    String bankAccountNumber,
    String contractType,
    String workSchedule,
    String shiftSystem,
    String shiftPattern,
    String position
) {
    public static EmployeeViewDto fromProjection(EmployeeViewProjection p) {
        return new EmployeeViewDto(
            p.getId(),
            p.getName(),
            p.getFatherSurname(),
            p.getMotherSurname(),
            p.getPhotoUrl(),
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
            p.getNationality(),
            p.getMaritalStatus(),
            p.getStudyLevel(),
            p.getPrevitionalSystem(),
            p.getPensionEntity(),
            p.getHealthSystem(),
            p.getHealthEntity(),
            p.getPaymentMethod(),
            p.getBank(),
            p.getBankAccountType(),
            p.getBankAccountNumber(),
            p.getContractType(),
            p.getWorkSchedule(),
            p.getShiftSystem(),
            p.getShiftPattern(),
            p.getPosition()
        );
    }
}
