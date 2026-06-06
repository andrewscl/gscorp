package com.gscorp.dv1.enums;

public enum UserStatus {
    INVITED("Invitado"),
    PENDING("Pendiente"),
    ACTIVE("Activo"),
    INACTIVE("Inactivo"),
    EXPIRED("Expirado"),
    SUSPENDED("Suspendido");

    private final String displayName;

    UserStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}