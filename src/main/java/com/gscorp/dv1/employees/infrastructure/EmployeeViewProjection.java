package com.gscorp.dv1.employees.infrastructure;

import java.time.LocalDate;

public interface EmployeeViewProjection {
    
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
    String getGender();
    String getNationality();
    String getMaritalStatus();
    String getStudyLevel();
    String getPrevitionalSystem();
    String getPensionEntity();
    String getHealthSystem();
    String getHealthEntity();
    String getPaymentMethod();
    String getBank();
    String getBankAccountType();
    String getBankAccountNumber();
    String getContractType();
    String getWorkSchedule();
    String getShiftSystem();
    String getShiftPattern();
    String getPosition();

}
