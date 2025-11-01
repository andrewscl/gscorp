package com.gscorp.dv1.employees.web.dto;

import java.time.LocalDate;
import java.util.Set;

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
import com.gscorp.dv1.shiftpatterns.infrastructure.ShiftPattern;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;

public record CreateEmployeeRequest(
    @NotBlank(message = "El nombre es obligatorio")
    String name,

    @NotBlank(message = "El apellido paterno es obligatorio")
    String fatherSurname,

    @NotBlank(message = "El apellido materno es obligatorio")
    String motherSurname,

    @NotBlank(message = "El RUT es obligatorio")
    String rut,

    @Email(message = "El email no es válido")
    String mail,

    @Pattern(regexp = "^\\+?\\d{0,3}?[- .]?\\d{1,4}[- .]?\\d{3,4}[- .]?\\d{3,4}$", message = "El teléfono no es válido")
    String phone,

    String secondaryPhone,

    Gender gender,

    Long nationalityId,

    MaritalStatus maritalStatus,

    StudyLevel studyLevel,

    Set<Long> professionIds,

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

    ShiftPattern shiftPattern,

    Long positionId,

    String password,

    String photoUrl,

    @PastOrPresent(message = "La fecha de ingreso debe ser pasada o actual")
    LocalDate hireDate,

    @Past(message = "La fecha de nacimiento debe ser pasada")
    LocalDate birthDate,

    LocalDate exitDate,

    Boolean active,

    String address,

    Long userId,

    Set<Long> projectIds
    
) {}