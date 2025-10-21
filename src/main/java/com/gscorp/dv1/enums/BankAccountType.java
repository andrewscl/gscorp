package com.gscorp.dv1.enums;

public enum BankAccountType {

    Vista("Cuenta Vista"),
    Corriente("Cuenta Corriente"),
    Ahorro("Cuenta de Ahorro"),
    Rut("Cuenta RUT"),
    Otro("Otro");

    private final String displayName;

    BankAccountType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
