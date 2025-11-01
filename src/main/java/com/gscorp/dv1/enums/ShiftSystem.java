package com.gscorp.dv1.enums;

public enum ShiftSystem {
    FIXED("Fijo"),
    ROTATING("Rotativo"),
    FLEXIBLE("Flexible"),
    FREE("Libre");

    private final String displayName;

    ShiftSystem(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
