package com.gscorp.dv1.enums;

public enum PaymentMethod {

    TRANSFER("Transferencia"),
    CASH("Efectivo"),
    CHECK("Cheque"),
    VALE_VISTA("Vale Vista");

    private final String displayName;

    PaymentMethod(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
