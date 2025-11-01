package com.gscorp.dv1.enums;

public enum BankAccountType {

    VISTA("Cuenta Vista"),
    CORRIENTE("Cuenta Corriente"),
    AHORRO("Cuenta de Ahorro"),
    RUT("Cuenta RUT"),
    OTRO("Otro");

    private final String displayName;

    BankAccountType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
