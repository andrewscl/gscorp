package com.gscorp.dv1.employees.web.dto;

import java.time.LocalDate;

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

    Long id, // ID del empleado
    String name, // Nombre del empleado
    String fatherSurname, // Apellido paterno
    String motherSurname, // Apellido materno
    String rut, // RUT
    String mail, // Correo
    String phone, // Teléfono
    String secondaryPhone, // Segundo teléfono

    Gender gender, // Enum: Género

    Long nationalityId, // ID de la nacionalidad relacionada
    String nationalityName, // Nombre de la nacionalidad relacionada

    MaritalStatus maritalStatus, // Enum: Estado Civil
    StudyLevel studyLevel, // Enum: Nivel de Estudio
    PrevitionalSystem previtionalSystem, // Enum: Sistema Previsional
    PensionEntity pensionEntity, // Enum: Entidad Previsional
    HealthSystem healthSystem, // Enum: Sistema de Salud
    HealthEntity healthEntity, // Enum: Entidad de Salud

    PaymentMethod paymentMethod, // Enum: Método de pago
    Long bankId, // ID del banco
    String bankName, // Nombre del banco
    BankAccountType bankAccountType, // Enum: Tipo de cuenta bancaria
    String bankAccountNumber, // Número de cuenta bancaria

    ContractType contractType, // Enum: Tipo de contrato
    WorkSchedule workSchedule, // Enum: Jornada laboral
    ShiftSystem shiftSystem, // Enum: Sistema de turnos

    Long shiftPatternId, // ID del patrón de turnos
    String shiftPatternName, // Nombre del patrón de turnos

    Long positionId, // ID del puesto
    String positionName, // Nombre del puesto

    String photoUrl, // URL de la foto

    LocalDate hireDate, // Fecha de contratación
    LocalDate birthDate, // Fecha de nacimiento
    LocalDate exitDate, // Fecha de salida

    Boolean active, // Estado activo/inactivo

    String address // Dirección

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
            p.getGender(),
            p.getNationalityId(),
            p.getNationalityName(),
            p.getMaritalStatus(),
            p.getStudyLevel(),
            p.getPrevitionalSystem(),
            p.getPensionEntity(),
            p.getHealthSystem(),
            p.getHealthEntity(),
            p.getPaymentMethod(),
            p.getBankId(),
            p.getBankName(),
            p.getBankAccountType(),
            p.getBankAccountNumber(),
            p.getContractType(),
            p.getWorkSchedule(),
            p.getShiftSystem(),
            p.getShiftPatternId(),
            p.getShiftPatternName(),
            p.getPositionId(),
            p.getPositionName(),
            p.getPhotoUrl(),
            p.getHireDate(),
            p.getBirthDate(),
            p.getExitDate(),
            p.getActive(),
            p.getAddress()
        );
    }
}
