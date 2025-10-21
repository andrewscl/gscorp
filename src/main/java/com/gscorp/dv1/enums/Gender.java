package com.gscorp.dv1.enums;

public enum Gender {
    Masculino("Masculino"),
    Femenino("Femenino"),
    Otro("Otro");

    private final String displayName;

    Gender(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }   
}