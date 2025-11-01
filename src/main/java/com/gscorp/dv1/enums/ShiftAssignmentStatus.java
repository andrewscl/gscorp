package com.gscorp.dv1.enums;

public enum ShiftAssignmentStatus {
    ASSIGNED("Asignado"),
    ABSENT("Ausente"),
    REPLACED("Reemplazado");

    private final String displayName;

    ShiftAssignmentStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
