package com.gscorp.dv1.employees.web.dto;

import java.time.LocalDate;
import java.util.Set;

import com.gscorp.dv1.projects.web.dto.ProjectDto;
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
    LocalDate hireDate,
    LocalDate birthDate,
    LocalDate exitDate,
    Boolean active,
    String address,
    // Relaciones
    UserDto user,
    Set<ProjectDto> projects
) {}