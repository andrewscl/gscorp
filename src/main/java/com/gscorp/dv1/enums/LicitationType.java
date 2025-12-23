package com.gscorp.dv1.enums;

import java.util.Arrays;

public enum LicitationType {
    L1("Licitación Pública < 100 UTM"),
    LE("Licitación Pública >= 100 UTM e < 1000 UTM"),
    LP("Licitación Pública > 1000 UTM e < 2000 UTM"),
    LQ("Licitación Pública >= 2000 UTM e < 5000 UTM"),
    LR("Licitación Pública >= 5000 UTM"),
    E2("Licitación Privada < 100 UTM"),
    CO("Licitación Privada >= 100 UTM e < 1000 UTM"),
    B2("Licitación Privada >= 1000 UTM e < 2000 UTM"),
    H2("Licitación Privada >= 2000 UTM e < 5000 UTM"),
    I2("Licitación Privada >= 5000 UTM"),
    LS("Licitación Pública de Servicios personales especializados"),
    UNKNOWN("Tipo de licitación desconocido");

    private final String description;

    LicitationType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static LicitationType fromString(String type) {
        return Arrays.stream(LicitationType.values())
                .filter(licitationType -> licitationType.name().equalsIgnoreCase(type))
                .findFirst()
                .orElse(UNKNOWN); // Default a UNKNOWN
    }

}
