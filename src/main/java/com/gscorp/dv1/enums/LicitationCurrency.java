package com.gscorp.dv1.enums;

import java.util.Arrays;

public enum LicitationCurrency {
    
    CLP("Peso chileno"),
    CLF("Unidad de Fomento"),
    USD("Dólar estadounidense"),
    UTM("Unidad Tributaria Mensual"),
    EUR("Euro"),
    UNKNOWN("Moneda desconocida");
    
    private final String description;

    LicitationCurrency(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    // Método para mapear valores desde cadenas recibidas en la API o el sistema
    public static LicitationCurrency fromString(String code) {
        if (code == null || code.isEmpty()) {
            return UNKNOWN; // Fallback si no hay valor
        }

        return Arrays.stream(LicitationCurrency.values())
                .filter(currency -> currency.name().equalsIgnoreCase(code))
                .findFirst()
                .orElse(UNKNOWN); // Default a UNKNOWN
    }

}
