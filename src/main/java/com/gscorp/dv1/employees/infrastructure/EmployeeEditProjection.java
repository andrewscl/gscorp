package com.gscorp.dv1.employees.infrastructure;

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

public interface EmployeeEditProjection {

    Long getId();
    String getName();
    String getFatherSurname();
    String getMotherSurname();
    String getRut();
    String getMail();
    String getPhone();
    String getSecondaryPhone();
    LocalDate getHireDate();
    LocalDate getBirthDate();
    LocalDate getExitDate();
    String getAddress();
    Boolean getActive();
    Gender getGender();
    Long getNationalityId();
    MaritalStatus getMaritalStatus();
    StudyLevel getStudyLevel();
    PrevitionalSystem getPrevitionalSystem();
    PensionEntity getPensionEntity();
    HealthSystem getHealthSystem();
    HealthEntity getHealthEntity();
    PaymentMethod getPaymentMethod();
    Long getBankId();
    BankAccountType getBankAccountType();
    String getBankAccountNumber();
    ContractType getContractType();
    WorkSchedule getWorkSchedule();
    ShiftSystem getShiftSystem();
    Long getShiftPatternId();
    Long getPositionId();
    Set<Long> getProjectIds();

}
