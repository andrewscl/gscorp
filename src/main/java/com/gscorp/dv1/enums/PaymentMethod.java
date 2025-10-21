package com.gscorp.dv1.enums;

public enum PaymentMethod {

    Transferencia("Transferencia"),
    Efectivo("Efectivo"),
    Cheque("Cheque"),
    Vale_Vista("Vale Vista");

    private final String displayName;

    PaymentMethod(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
