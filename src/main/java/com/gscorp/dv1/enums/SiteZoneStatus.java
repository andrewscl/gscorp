package com.gscorp.dv1.enums;

public enum SiteZoneStatus {
    ACTIVE ("Activo"),
    INACTIVE ("Inactivo"),
    SUSPENDED ("Suspendido");

    private final String displayName;

    SiteZoneStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName(){
        return displayName;
    }

}
