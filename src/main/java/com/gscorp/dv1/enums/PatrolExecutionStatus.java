package com.gscorp.dv1.enums;

public enum PatrolExecutionStatus {
    
    IN_PROGRESS("En progreso"),
    COMPLETED("Completada"),
    ABORTED("Abortada");

    private final String displayName;

    PatrolExecutionStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

}
