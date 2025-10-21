package com.gscorp.dv1.enums;

public enum MaritalStatus {
    SOLTERO("Soltero/a"),
    CASADO("Casado/a"),
    DIVORCIADO("Divorciado/a"),
    VIUDO("Viudo/a"),
    UNION_LIBRE("Unión Libre");
    
    private final String displayName;

    MaritalStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
