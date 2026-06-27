package com.gscorp.dv1.employees.infrastructure.Projections;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import com.gscorp.dv1.enums.EmployeeStatus;

public interface EmployeeTableProjection {
    Long getId();
    UUID getExternalId();
    String getPhotoUrl();
    String getName();
    String getFatherSurname();
    String getMotherSurname();
    String getRut();
    String getMail();
    String getPhone();
    String getPositionName();
    EmployeeStatus getStatus();
    LocalDate getHireDate();
    LocalDateTime getCreatedAt();
    String getUsername();
    String getUserMail();
    String getUserPhone();

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
