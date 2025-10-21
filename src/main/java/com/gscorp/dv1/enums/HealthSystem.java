package com.gscorp.dv1.enums;

public enum HealthSystem {
    FONASA("Fonasa"),
    ISAPRE("Isapre"),
    OTHER("Otro");

    private final String displayName;

    HealthSystem(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
