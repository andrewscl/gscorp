package com.gscorp.dv1.shiftrequests.infrastructure;

import com.gscorp.dv1.enums.RequestType;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Convierte RequestType <-> String usando displayName para persistencia,
 * y acepta tanto displayName como name() al leer (tolerante).
 */
@Converter(autoApply = false)
public class RequestTypeConverter implements AttributeConverter<RequestType, String> {

    @Override
    public String convertToDatabaseColumn(RequestType attribute) {
        return attribute == null ? null : attribute.getDisplayName();
    }

    @Override
    public RequestType convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;

        // Primero intentar por displayName exacto
        for (RequestType t : RequestType.values()) {
            if (t.getDisplayName().equals(dbData)) {
                return t;
            }
        }

        // Si no coincide por displayName, intentar por name() (por compatibilidad)
        try {
            return RequestType.valueOf(dbData.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Valor desconocido para RequestType en BD: " + dbData, ex);
        }
    }
}