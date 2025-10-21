package com.gscorp.dv1.enums;

public enum MaritalStatus {
    Soltero("Soltero/a"),
    Casado("Casado/a"),
    Divorciado("Divorciado/a"),
    Viudo("Viudo/a"),
    Union_Libre("Unión Libre");

    private final String displayName;

    MaritalStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
