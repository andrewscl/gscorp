package com.gscorp.dv1.employees.web.dto;

import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

import com.gscorp.dv1.employees.infrastructure.Employee;
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
import com.gscorp.dv1.positions.web.dto.PositionDto;
import com.gscorp.dv1.professions.web.dto.ProfessionDto;
import com.gscorp.dv1.projects.web.dto.ProjectDto;
import com.gscorp.dv1.shiftpatterns.infrastructure.ShiftPattern;
import com.gscorp.dv1.users.web.dto.UserDto;

public record EmployeeDto(
    Long id,
    String name,
    String motherSurname,
    String fatherSurname,
    String rut,
    String mail,
    String phone,
    String secondaryPhone,
    Gender gender,
    Long nationalityId,
    String nationalityName,
    MaritalStatus maritalStatus,
    StudyLevel studyLevel,
    Set<ProfessionDto> professions,
    PrevitionalSystem previtionalSystem,
    PensionEntity pensionEntity,
    HealthSystem healthSystem,
    HealthEntity healthEntity,
    PaymentMethod paymentMethod,
    Long bankId,
    String bankName,
    BankAccountType bankAccountType,
    String bankAccountNumber,
    ContractType contractType,
    WorkSchedule workSchedule,
    ShiftSystem shiftSystem,
    ShiftPattern shiftPattern,
    PositionDto position,
    String photoUrl,
    LocalDate hireDate,
    LocalDate birthDate,
    LocalDate exitDate,
    Boolean active,
    String address,
    UserDto user,
    Set<ProjectDto> projects
) {
    public static EmployeeDto fromEntity(Employee e) {
        return new EmployeeDto(
            e.getId(),
            e.getName(),
            e.getMotherSurname(),
            e.getFatherSurname(),
            e.getRut(),
            e.getMail(),
            e.getPhone(),
            e.getSecondaryPhone(),
            e.getGender(),
            e.getNationality() != null ? e.getNationality().getId() : null,
            e.getNationality() != null ? e.getNationality().getName() : null,
            e.getMaritalStatus(),
            e.getStudyLevel(),
            e.getProfessions() != null ? e.getProfessions().stream().map(ProfessionDto::fromEntity).collect(Collectors.toSet()) : Set.of(),
            e.getPrevitionalSystem(),
            e.getPensionEntity(),
            e.getHealthSystem(),
            e.getHealthEntity(),
            e.getPaymentMethod(),
            e.getBank() != null ? e.getBank().getId() : null,
            e.getBank() != null ? e.getBank().getName() : null,
            e.getBankAccountType(),
            e.getBankAccountNumber(),
            e.getContractType(),
            e.getWorkSchedule(),
            e.getShiftSystem(),
            e.getShiftPattern(),
            e.getPosition() != null ? PositionDto.fromEntity(e.getPosition()) : null,
            e.getPhotoUrl(),
            e.getHireDate(),
            e.getBirthDate(),
            e.getExitDate(),
            e.getActive(),
            e.getAddress(),
            e.getUser() != null ? UserDto.fromEntity(e.getUser()) : null,
            e.getProjects() != null ? e.getProjects().stream().map(ProjectDto::fromEntity).collect(Collectors.toSet()) : Set.of()
        );
    }
}