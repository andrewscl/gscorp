package com.gscorp.dv1.enums;

public enum AccountType {

    HOLDING("Holding"),
    COMPANY("Company"),
    CLIENT("Client");

    private final String displayName;

    AccountType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

}
