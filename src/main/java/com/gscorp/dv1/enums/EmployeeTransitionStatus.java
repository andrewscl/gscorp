package com.gscorp.dv1.enums;

public enum EmployeeTransitionStatus {
    PENDING("Pendiente"),
    IN_PROGRESS("En proceso"),
    COMPLETED("Completado"),
    REJECTED("Rechazado");

    private final String displayName;

    EmployeeTransitionStatus(String displayName) {
        this.displayName = displayName;
    }
    public String getDisplayName() {
        return displayName;
    }
}
