package com.gscorp.dv1.enums;

public enum Units {
    COUNT ("Cantidad"),
    HOURS ("Horas"),
    PERCENTAGE ("Porcentaje"),
    UNITS ("Unidades");

    private final String displayName;

    Units(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

}