package com.gscorp.dv1.employees.infrastructure;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface EmployeeTableProjection {
    Long getId();
    String getPhotoUrl();
    String getName();
    String getFatherSurname();
    String getMotherSurname();
    String getRut();
    String getMail();
    String getPhone();
    String getPositionName();
    Boolean getActive();
    LocalDate getHireDate();
    LocalDateTime getCreatedAt();

    // conveniencia
    default String getFullName() {
        StringBuilder sb = new StringBuilder();
        if (getName() != null) sb.append(getName());
        if (getFatherSurname() != null) sb.append(" ").append(getFatherSurname());
        if (getMotherSurname() != null) sb.append(" ").append(getMotherSurname());
        String s = sb.toString().trim();
        return s.isEmpty() ? "—" : s;
    }
}
