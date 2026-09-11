package com.gscorp.dv1.enums;

public enum SiteStatus {
    ACTIVE ("Activo"),
    INACTIVE ("Inactivo"),
    MAINTENANCE ("En mantenimiento"),
    SUSPENDED ("Suspendido");
    
    private final String displayName;

    SiteStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName(){
        return displayName;
    }
}
