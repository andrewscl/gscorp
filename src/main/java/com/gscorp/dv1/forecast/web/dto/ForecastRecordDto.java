package com.gscorp.dv1.forecast.web.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;

import org.hibernate.proxy.HibernateProxy;

import com.gscorp.dv1.enums.ForecastMetric;
import com.gscorp.dv1.enums.Periodicity;
import com.gscorp.dv1.enums.Units;
import com.gscorp.dv1.forecast.infrastructure.Forecast;
import com.gscorp.dv1.users.infrastructure.User;

public record ForecastRecordDto (
    Long id,
    Long clientId,
    Long projectId,
    Long siteId,
    ForecastMetric forecastMetric,
    Periodicity periodicity,
    OffsetDateTime periodStart,
    OffsetDateTime periodEnd,
    Integer periodStartHour,
    Integer periodEndHour,
    BigDecimal value,
    Units units,
    String tz,
    String note,
    BigDecimal confidence,
    Integer forecastVersion,
    Long createdBy,
    Long updatedBy,
    Instant createdAt,
    Instant updatedAt,
    Boolean isActive,
    Long rowVersion
){
    public static ForecastRecordDto fromEntity(Forecast entity) {
        if (entity == null) return null;

        Long createdById = extractUserId(entity.getCreatedBy());
        Long updatedById = extractUserId(entity.getUpdatedBy());

        return new ForecastRecordDto(
            entity.getId(),
            entity.getClientId(),
            entity.getProjectId(),
            entity.getSiteId(),
            entity.getForecastMetric(),
            entity.getPeriodicity(),
            entity.getPeriodStart(),
            entity.getPeriodEnd(),
            entity.getPeriodStartHour(),
            entity.getPeriodEndHour(),
            entity.getValue(),
            entity.getUnits(),
            entity.getTz(),
            entity.getNote(),
            entity.getConfidence(),
            entity.getForecastVersion(),
            createdById,
            updatedById,
            entity.getCreatedAt(),
            entity.getUpdatedAt(),
            entity.getIsActive(),
            entity.getRowVersion()
        );
    }

    /**
     * Extrae de forma segura el id de una propiedad que puede ser:
     * - Long (id directo)
     * - User (entidad)
     * - HibernateProxy (proxy de User)
     * - null
     */
    private static Long extractUserId(Object obj) {
        if (obj == null) return null;
        // caso: ya es Long
        if (obj instanceof Long) return (Long) obj;
        // caso: entidad User directamente
        if (obj instanceof User) {
            return ((User) obj).getId();
        }
        // caso: Hibernate proxy (ej. User$HibernateProxy)
        if (obj instanceof HibernateProxy) {
            try {
                Object idObj = ((HibernateProxy) obj).getHibernateLazyInitializer().getIdentifier();
                if (idObj == null) return null;
                if (idObj instanceof Number) return ((Number) idObj).longValue();
                return Long.valueOf(idObj.toString());
            } catch (Exception e) {
                return null;
            }
        }
        // intento por reflexión (último recurso)
        try {
            var method = obj.getClass().getMethod("getId");
            Object idObj = method.invoke(obj);
            if (idObj == null) return null;
            if (idObj instanceof Number) return ((Number) idObj).longValue();
            if (idObj instanceof Long) return (Long) idObj;
            return Long.valueOf(idObj.toString());
        } catch (Exception e) {
            // ignore y devolver null
        }
        return null;
    }
}