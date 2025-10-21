package com.gscorp.dv1.enums;

public enum ShiftSystem {
    Fijo("Fijo"),
    Rotativo("Rotativo"),
    Flexible("Flexible"),
    Libre("Libre");

    private final String displayName;

    ShiftSystem(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
