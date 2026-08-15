package com.gscorp.dv1.enums;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum ShiftRequestType {
    SPORADIC("Esporádico"),
    FIXED("Fijo");

    private final String displayName;

    ShiftRequestType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getName() {
        return this.name();
    }

}