package com.gscorp.dv1.employees.infrastructure;

import java.time.LocalDate;

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

public interface EmployeeEditProjection {

    Long getId(); // ID del empleado
    String getName(); // Nombre del empleado
    String getFatherSurname(); // Apellido paterno
    String getMotherSurname(); // Apellido materno
    String getRut(); // RUT
    String getMail(); // Correo
    String getPhone(); // Teléfono
    String getSecondaryPhone(); // Segundo teléfono

    Gender getGender(); // Enum: Género

    Long getNationalityId(); // ID de la nacionalidad relacionada
    String getNationalityName(); // Nombre de la nacionalidad relacionada

    MaritalStatus getMaritalStatus(); // Enum: Estado Civil
    StudyLevel getStudyLevel(); // Enum: Nivel de Estudio

    PrevitionalSystem getPrevitionalSystem(); // Enum: Sistema Previsional
    PensionEntity getPensionEntity(); // Enum: Entidad Previsional
    HealthSystem getHealthSystem(); // Enum: Sistema de Salud
    HealthEntity getHealthEntity(); // Enum: Entidad de Salud

    PaymentMethod getPaymentMethod(); // Enum: Método de pago
    Long getBankId(); // ID del banco
    String getBankName(); // Nombre del banco
    BankAccountType getBankAccountType(); // Enum: Tipo de cuenta bancaria
    String getBankAccountNumber(); // Número de cuenta bancaria

    ContractType getContractType(); // Enum: Tipo de contrato
    WorkSchedule getWorkSchedule(); // Enum: Jornada laboral
    ShiftSystem getShiftSystem(); // Enum: Sistema de turnos

    Long getShiftPatternId(); // ID del patrón de turnos
    String getShiftPatternName(); // Nombre del patrón de turnos

    Long getPositionId(); // ID del puesto
    String getPositionName(); // Nombre del puesto

    String getPhotoUrl(); // URL de la foto

    LocalDate getHireDate(); // Fecha de contratación
    LocalDate getBirthDate(); // Fecha de nacimiento
    LocalDate getExitDate(); // Fecha de salida

    Boolean getActive(); // Estado activo/inactivo

    String getAddress(); // Dirección

}
