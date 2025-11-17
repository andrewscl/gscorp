package com.gscorp.dv1.enums;

import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

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

    /**
     * Map de alias a enums para deserialización tolerante.
     * Añade tantas variantes como necesites aquí.
     */
    private static final Map<String, RequestType> ALIASES = Stream.of(new Object[][]{
            // valores canónicos (nombre del enum)
            {"ESPORADIC", ESPORADIC},
            {"FIXED", FIXED},

            // alias en español / sin acentos / sin tildes / traducciones
            {"ESPORÁDICO", ESPORADIC},
            {"ESPORADICO", ESPORADIC},
            {"ADICIONAL", ESPORADIC},
            {"FIJO", FIXED},

            // posibles entradas en inglés
            {"ADDITIONAL", ESPORADIC}
    }).collect(Collectors.toMap(kv -> ((String) kv[0]).toUpperCase(Locale.ROOT), kv -> (RequestType) kv[1]));

    @JsonCreator
    public static RequestType from(String v) {
        if (v == null) return null;
        String key = v.trim().toUpperCase(Locale.ROOT);
        RequestType rt = ALIASES.get(key);
        if (rt != null) return rt;

        // fallback: intentar name() directo (por si viene "Esporadic" mal capitalizado)
        try {
            return RequestType.valueOf(key);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("RequestType inválido: " + v);
        }
    }

    /**
     * Serializa el enum con su nombre (por defecto). Puedes cambiar a getDisplayName()
     * si prefieres que la API devuelva la representación legible en lugar del nombre.
     */
    @JsonValue
    public String toValue() {
        return name();
    }
}