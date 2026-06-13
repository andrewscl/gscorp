package com.gscorp.dv1.employees.infrastructure.Projections;

import java.time.LocalDate;
import java.util.UUID;

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

public interface EmployeeViewProjection {
    
    Long getId();
    UUID getExternalId();
    String getName();
    String getFatherSurname();
    String getMotherSurname();
    String getPhotoUrl();
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
    String getNationality();
    MaritalStatus getMaritalStatus();
    StudyLevel getStudyLevel();
    PrevitionalSystem getPrevitionalSystem();
    PensionEntity getPensionEntity();
    HealthSystem getHealthSystem();
    HealthEntity getHealthEntity();
    PaymentMethod getPaymentMethod();
    String getBank();
    BankAccountType getBankAccountType();
    String getBankAccountNumber();
    ContractType getContractType();
    WorkSchedule getWorkSchedule();
    ShiftSystem getShiftSystem();
    String getShiftPattern();
    String getPosition();
    String getCompany();
    EmployeeStatus getStatus();
}
