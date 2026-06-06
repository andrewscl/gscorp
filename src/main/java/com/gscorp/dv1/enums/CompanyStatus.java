package com.gscorp.dv1.enums;

public enum CompanyStatus {
    ACTIVE ("Activa"),
    INACTIVE ("Inactiva");

    private final String displayName;

    CompanyStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
