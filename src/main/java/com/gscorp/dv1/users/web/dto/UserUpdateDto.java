package com.gscorp.dv1.users.web.dto;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * DTO para actualizar un User. Campos no nulos se aplican (merge parcial).
 * Ajusta los campos según necesidades de negocio.
 */
public record UserUpdateDto(
    String username,
    String mail,
    Boolean active,
    Long roleId,
    Set<Long> companyIds,
    Set<Long> clientIds,
    Long employeeId,
    String timeZone
) {
    public static UserUpdateDto fromJson(JsonNode body) {

        return new UserUpdateDto(
            parseString(body, "username", true),
            parseString(body, "mail", true),
            parseBoolean(body, "active"),
            parseLongOrTextual(body, "roleId"),
            parseLongSet(body, "companyIds"),
            parseLongSet(body, "clientIds"),
            parseLongOrTextual(body, "employeeId"),
            parseString(body, "timeZone", true)
        );
    }

    private static String parseString(JsonNode body, String fieldName, boolean trim) {
        if (!body.has(fieldName)) return null;
        JsonNode n = body.get(fieldName);
        if (n.isNull()) return null;
        String value = n.asText();
        return trim ? value.trim() : value;
    }

    private static Boolean parseBoolean(JsonNode body, String fieldName) {
        if (!body.has(fieldName)) return null;
        JsonNode n = body.get(fieldName);
        return n.isNull() ? null : n.asBoolean();
    }

    private static Set<Long> parseLongSet(JsonNode body, String fieldName) {
        if (!body.has(fieldName)) return null;
        JsonNode arr = body.get(fieldName);
        return parseLongSetFromArrayNode(arr);
    }

    private static Set<Long> parseLongSetFromArrayNode(JsonNode arr) {
        if (arr == null || arr.isNull()) return null;
        if (!arr.isArray()) throw new IllegalArgumentException("se esperaba un array de ids");
        Set<Long> out = new HashSet<>();
        Iterator<JsonNode> it = arr.elements();
        while (it.hasNext()) {
            JsonNode n = it.next();
            if (n == null || n.isNull()) continue;
            if (n.canConvertToLong()) {
                out.add(n.asLong());
            } else if (n.isTextual() && !n.asText().isBlank()) {
                try {
                    out.add(Long.valueOf(n.asText().trim()));
                } catch (NumberFormatException ex) {
                    throw new IllegalArgumentException("id inválido en array");
                }
            }
        }
        return out;
    }

    private static Long parseLongOrTextual(JsonNode body, String fieldName) {
        if (!body.has(fieldName)) return null;
        JsonNode n = body.get(fieldName);
        if (n.isNull()) return null;
        if (n.canConvertToLong()) {
            return n.asLong();
        } else if (n.isTextual()) {
            String textValue = n.asText().trim();
            if (!textValue.isBlank()) {
                try {
                    return Long.parseLong(textValue);
                } catch (NumberFormatException ex) {
                    throw new IllegalArgumentException(fieldName + " inválido: " + textValue);
                }
            }
        }

        return null; // Valor no válido o textualmente vacío
    }

}