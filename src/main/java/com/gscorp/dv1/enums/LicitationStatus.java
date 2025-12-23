package com.gscorp.dv1.enums;

import java.util.Arrays;

public enum LicitationStatus {
    PUBLISHED("Publicada"),                                       // Publicada
    CLOSED("Cerrada"),                                            // Cerrada
    DESERTED("Desierta (Art. 3 o 9 Ley 19.886)"),                 // Desierta con cláusula legal
    AWARDED("Adjudicada"),                                        // Adjudicada
    REAWARDED("Readjudicada"),                                    // Readjudicada
    AWARDED_ORDER_REJECTED("Adjudicada con Orden de Compra Rechazadas"), // Adjudicada con problemas de orden de compra
    AUTHORIZED_FOR_AWARD("Autorizada para Adjudicación"),         // Autorizada para adjudicación
    SENT_TO_AUTHORIZATION_FOR_AWARD("Enviada a Autorizar para Adjudicación"), // Enviada para autorizar adjudicación
    AUTHORIZED_FOR_DESERTION("Autorizada para Deserción"),        // Autorizada para deserción
    SENT_TO_AUTHORIZATION_FOR_DESERTION("Enviada a Autorizar para Deserción"), // Enviada para autorizar deserción
    REVOKED("Revocada"),                                          // Revocada
    SUSPENDED("Suspendida"),                                      // Suspendida
    UNKNOWN("Estado desconocido");                                // Estado desconocido como fallback

    private final String description;

    LicitationStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    // Método para mapear valores desde cadenas recibidas en la API o el sistema
    public static LicitationStatus fromString(String status) {
        if (status == null || status.isEmpty()) {
            return UNKNOWN; // Fallback si no hay valor
        }

        return Arrays.stream(LicitationStatus.values())
                .filter(licitationStatus -> licitationStatus.description.equalsIgnoreCase(status))
                .findFirst()
                .orElse(UNKNOWN); // Default a UNKNOWN
    }
}
