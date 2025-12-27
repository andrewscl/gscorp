package com.gscorp.dv1.employees.web.dto;

import java.time.LocalDate;
import java.util.Set;

import com.gscorp.dv1.employees.infrastructure.EmployeeEditProjection;
import com.gscorp.dv1.enums.BankAccountType;
import com.gscorp.dv1.enums.ContractType;
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
    PrevitionalSystem previtionalSystem,
    PensionEntity pensionEntity,
    HealthSystem healthSystem,
    HealthEntity healthEntity,
    PaymentMethod paymentMethod,
    Long bankId,
    BankAccountType bankAccountType,
    String bankAccountNumber,
    ContractType contractType,
    WorkSchedule workSchedule,
    ShiftSystem shiftSystem,
    Long shiftPatternId,
    Long positionId,
    Set<Long> projectIds

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
            p.getPrevitionalSystem(),
            p.getPensionEntity(),
            p.getHealthSystem(),
            p.getHealthEntity(),
            p.getPaymentMethod(),
            p.getBankId(),
            p.getBankAccountType(),
            p.getBankAccountNumber(),
            p.getContractType(),
            p.getWorkSchedule(),
            p.getShiftSystem(),
            p.getShiftPatternId(),
            p.getPositionId(),
            p.getProjectIds()
        );
    }
}
