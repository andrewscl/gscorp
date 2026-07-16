package com.gscorp.dv1.enums;

public enum EmployeeTransitionRequestStatus {
    PENDING("Pendiente"),
    IN_PROGRESS("En proceso"),
    COMPLETED("Completado"),
    REJECTED("Rechazado");

    private final String displayName;

    EmployeeTransitionRequestStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
