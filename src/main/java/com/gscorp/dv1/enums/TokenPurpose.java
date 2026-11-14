package com.gscorp.dv1.enums;

public enum TokenPurpose {
    INVITATION("Invitación"),
    PASSWORD_RECOVERY("Recuperación de contraseña");

    private final String displayName;

    TokenPurpose(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
