package com.gscorp.dv1.enums;

public enum ShiftRequestStatus {
    REQUESTED("Solicitado"),
    APPROVED("Aprobado"),
    CANCELLED("Cancelado");

    private final String displayName;

    ShiftRequestStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
