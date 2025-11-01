package com.gscorp.dv1.enums;

public enum PensionEntity {

    CAPITAL("Capital"),
    CUPRUM("Cuprum"),
    HABITAT("Habitat"),
    MODELO("Modelo"),
    PLANVITAL("PlanVital"),
    PROVIDA("Provida"),
    UNO("Uno");

    private final String displayName;

    PensionEntity(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
    
}
