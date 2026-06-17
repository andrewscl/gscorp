package com.gscorp.dv1.enums;

public enum PatrolScheduleStatus {
    SCHEDULED("Programada"),
    IN_PROGRESS("En progreso"),
    COMPLETED("Completada"),
    MISSED("No realizada");

    private final String displayName;

    PatrolScheduleStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName(){
        return displayName;
    }
}
