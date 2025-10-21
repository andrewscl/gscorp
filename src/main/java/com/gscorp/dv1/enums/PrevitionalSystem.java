package com.gscorp.dv1.enums;

public enum PrevitionalSystem {
    AFP("AFP"),
    IPS("IPS"),
    Otro("Otro");

    private final String displayName;

    PrevitionalSystem(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
    
}
