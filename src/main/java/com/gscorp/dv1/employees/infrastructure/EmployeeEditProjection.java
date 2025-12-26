package com.gscorp.dv1.employees.infrastructure;

import java.time.LocalDate;

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

}
