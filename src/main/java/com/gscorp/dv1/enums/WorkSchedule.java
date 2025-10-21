package com.gscorp.dv1.enums;

public enum WorkSchedule {
    Dayly("Diurno"),
    Nightly("Nocturno"),
    Mixed("Mixto"),
    Flexible("Flexible"),
    Other("Otro");

    private final String displayName;

    WorkSchedule(String displayName) {
        this.displayName = displayName;
    }
    public String getDisplayName() {
        return displayName;
    }
}
