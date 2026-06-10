package com.gscorp.dv1.enums;

public enum ClientStatus {
    ACTIVE ("Activa"),
    INACTIVE ("Inactiva");

    private final String displayName;

    ClientStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName(){
        return displayName;
    }
    
}
