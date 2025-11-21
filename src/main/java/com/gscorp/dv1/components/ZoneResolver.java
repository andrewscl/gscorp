package com.gscorp.dv1.components;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Optional;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import com.gscorp.dv1.components.dto.ZoneResolutionResult;
import com.gscorp.dv1.users.application.UserService;

import lombok.RequiredArgsConstructor;


@Component
@RequiredArgsConstructor
public class ZoneResolver {

    private static final Logger log = LoggerFactory.getLogger(ZoneResolver.class);

    private final UserService userService;

    /**
     * Resuelve la ZoneId siguiendo la prioridad:
     * 1) requestedZone (si es válida)
     * 2) userService.getUserZone(userId) (si existe y es válida)
     * 3) system default
     *
     * Devuelve ZoneResolutionResult con el ZoneId ya listo para usar.
     */
    public ZoneResolutionResult resolveZone(Long userId, String requestedZone) {
        // 1) requested zone (si viene, intentar parsearla)
        if (requestedZone != null && !requestedZone.isBlank()) {
            try {
                ZoneId z = ZoneId.of(requestedZone);
                return new ZoneResolutionResult(z, ZoneResolutionResult.SOURCE_REQUESTED);
            } catch (DateTimeException ex) {
                log.warn("Zona solicitada inválida '{}' para userId={}", requestedZone, userId);
                // continuar al siguiente fallback
            }
        }

        // 2) zona del perfil de usuario (UserService devuelve Optional<ZoneId>)
        if (userId != null) {
            try {
                Optional<ZoneId> userZoneOpt = userService.getUserZone(userId);
                if (userZoneOpt.isPresent()) {
                    return new ZoneResolutionResult(userZoneOpt.get(), ZoneResolutionResult.SOURCE_USER);
                }
            } catch (Exception ex) {
                // No queremos que una excepción en userService rompa la resolución; registramos y fallback
                log.debug("No se pudo obtener zone del userService para userId={}: {}", userId, ex.getMessage());
            }
        }

        // 3) fallback final: system default
        return new ZoneResolutionResult(ZoneId.systemDefault(), ZoneResolutionResult.SOURCE_SYSTEM);
    }

    /** Convierte LocalDate -> OffsetDateTime al inicio del día en la zona dada. */
    public OffsetDateTime toStartOfDay(LocalDate date, ZoneId zone) {
        return date.atStartOfDay(zone).toOffsetDateTime();
    }

    /** Convierte LocalDate -> OffsetDateTime al final del día (inclusive) en la zona dada. */
    public OffsetDateTime toEndOfDayInclusive(LocalDate date, ZoneId zone) {
        return date.plusDays(1).atStartOfDay(zone).minusNanos(1).toOffsetDateTime();
    }

}
