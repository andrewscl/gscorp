package com.gscorp.dv1.enums;

public enum EmployeeRequestStatusType {
    TERMINATION("Desvinculación");

    private final String displayName;

    EmployeeRequestStatusType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
