package com.gscorp.dv1.enums;

public enum HrProcessType {
    ONBOARDING("Contratación"),
    TERMINATION("Desvinculación"),
    PROMOTION("Ascenso"),
    ABSENCE("Ausencia");

    private final String displayName;

    HrProcessType(String displayName) {
        this.displayName = displayName;
    }
    public String getDisplayName() {
        return displayName;
    }
}
