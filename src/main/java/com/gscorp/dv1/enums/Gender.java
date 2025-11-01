package com.gscorp.dv1.enums;

public enum Gender {
    MALE("Masculino"),
    FEMALE("Femenino"),
    OTHER("Otro");

    private final String displayName;

    Gender(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }   
}