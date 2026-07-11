package com.gscorp.dv1.hr.employees.web.dto;

import java.time.LocalDate;

import com.gscorp.dv1.enums.BankAccountType;
import com.gscorp.dv1.enums.ContractType;
import com.gscorp.dv1.enums.EmployeeStatus;
import com.gscorp.dv1.enums.Gender;
import com.gscorp.dv1.enums.HealthEntity;
import com.gscorp.dv1.enums.HealthSystem;
import com.gscorp.dv1.enums.MaritalStatus;
import com.gscorp.dv1.enums.PaymentMethod;
import com.gscorp.dv1.enums.PensionEntity;
import com.gscorp.dv1.enums.PrevitionalSystem;
import com.gscorp.dv1.enums.ShiftSystem;
import com.gscorp.dv1.enums.StudyLevel;
import com.gscorp.dv1.enums.WorkSchedule;
import com.gscorp.dv1.hr.employees.infrastructure.Projections.EmployeeViewProjection;

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
    Gender gender,
    String nationality,
    MaritalStatus maritalStatus,
    StudyLevel studyLevel,
    PrevitionalSystem previtionalSystem,
    PensionEntity pensionEntity,
    HealthSystem healthSystem,
    HealthEntity healthEntity,
    PaymentMethod paymentMethod,
    String bank,
    BankAccountType bankAccountType,
    String bankAccountNumber,
    ContractType contractType,
    WorkSchedule workSchedule,
    ShiftSystem shiftSystem,
    String shiftPattern,
    String position,
    String company,
    EmployeeStatus status
) {
    public static EmployeeViewDto
            fromProjection(EmployeeViewProjection p) {
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
            p.getPosition(),
            p.getCompany(),
            p.getStatus()
        );
    }
}
