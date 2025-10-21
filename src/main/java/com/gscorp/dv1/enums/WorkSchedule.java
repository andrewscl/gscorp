package com.gscorp.dv1.enums;

public enum WorkSchedule {
    Diurno("Diurno"),
    Nocturno("Nocturno"),
    Mixto("Mixto"),
    Flexible("Flexible");

    private final String displayName;

    WorkSchedule(String displayName) {
        this.displayName = displayName;
    }
    public String getDisplayName() {
        return displayName;
    }
}
