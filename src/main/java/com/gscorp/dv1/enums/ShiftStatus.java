package com.gscorp.dv1.enums;

public enum ShiftStatus {
    PLANNED("Planificado"),
    IN_PROGRESS("En Progreso"),
    COMPLETED("Completado"),
    CANCELLED("Cancelado"),
    PENDING("Pendiente");

    private final String displayName;

    ShiftStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}