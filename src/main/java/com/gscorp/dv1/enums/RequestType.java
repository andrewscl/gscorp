package com.gscorp.dv1.enums;

/**
 * Enum para tipos de solicitud. Se mantiene displayName para UI y se añade
 * soporte de deserialización tolerante con @JsonCreator para aceptar valores
 * en español/inglés y variantes.
 */
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