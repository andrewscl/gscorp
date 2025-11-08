package com.gscorp.dv1.enums;

public enum RequestType {
    ESPORADIC("Esporádico"),
    FIXED("Fijo");

    private final String displayName;

    RequestType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

}
