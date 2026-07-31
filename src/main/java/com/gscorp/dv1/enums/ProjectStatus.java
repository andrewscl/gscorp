package com.gscorp.dv1.enums;

public enum ProjectStatus {
    PLANNING("En Planificación"),
    ACTIVE("Activo"),
    SUSPENDED("Suspendido"),
    COMPLETED("Finalizado");

    private final String displayName;

    ProjectStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
