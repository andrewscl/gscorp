package com.gscorp.dv1.employees.web.dto;

import java.time.LocalDate;
import java.util.Set;

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

    String gender,

    String nationality,

    String maritalStatus,

    String studyLevel,

    String profession,

    String previtionalSystem,

    String healthSystem,

    String paymentMethod,

    String bankId,

    String bankName,

    String bankAccountType,

    String bankAccountNumber,

    String contractType,

    String workSchedule,

    String shiftSystem,

    String position,

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