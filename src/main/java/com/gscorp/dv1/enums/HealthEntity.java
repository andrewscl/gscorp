package com.gscorp.dv1.enums;

public enum HealthEntity {

    COLMENA("Colmena"),
    BANMEDICA("Banmédica"),
    CONSALUD("Consalud"),
    MASVIDA("Masvida"),
    CRUZBLANCA("Cruz Blanca"),
    FONASA("Fonasa");

    private final String displayName;

    HealthEntity(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
    
}
