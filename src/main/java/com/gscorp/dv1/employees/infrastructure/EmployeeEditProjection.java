package com.gscorp.dv1.employees.infrastructure;

import java.time.LocalDate;

import com.gscorp.dv1.enums.Gender;
import com.gscorp.dv1.enums.MaritalStatus;
import com.gscorp.dv1.enums.StudyLevel;

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
    Long getProfessionId();

}
