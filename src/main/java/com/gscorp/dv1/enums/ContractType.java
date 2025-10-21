package com.gscorp.dv1.enums;

public enum ContractType {
    Indefinido("Indefinido"),
    Plazo_Fijo("Plazo Fijo"),
    Honorarios("Honorarios"),
    Part_Time("Part-Time"),
    Practica("Práctica"),
    Otro("Otro");

    private final String displayName;

    ContractType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

}
