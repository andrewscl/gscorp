package com.gscorp.dv1.enums;

public enum Periodicity {
    HOURLY("Horaria"),
    DAILY("Diaria"),
    WEEKLY("Semanal"),
    MONTHLY("Mensual"),
    YEARLY("Anual");

    private final String displayName;

    Periodicity(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
