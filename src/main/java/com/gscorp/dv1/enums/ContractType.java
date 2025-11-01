package com.gscorp.dv1.enums;

public enum ContractType {
    INDEFINITE("Indefinido"),
    FIXED_TERM("Plazo Fijo"),
    HONORARIOS("Honorarios"),
    PART_TIME("Part-Time"),
    INTERNSHIP("Práctica"),
    OTHER("Otro");

    private final String displayName;

    ContractType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

}
