package com.gscorp.dv1.patrol.application;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collection;
import java.util.List;

import com.gscorp.dv1.patrol.infrastructure.PatrolRunRepo;
import com.gscorp.dv1.patrol.web.dto.PatrolHourlyDto;

public interface PatrolRunService {

    /**
     * Compliance para varios clientIds en el rango [from, to] usando la zona indicada.
     * Devuelve null si no hay datos (por ejemplo expected = 0).
     */
    Double compliance(Collection<Long> clientIds, LocalDate from, LocalDate to, ZoneId zone);

    /**
     * Conveniencia: compliance para un único clientId (envuelve y delega en la variante multi).
     */
    default Double compliance(Long clientId, LocalDate from, LocalDate to, ZoneId zone) {
        return compliance(clientId == null ? null : java.util.List.of(clientId), from, to, zone);
    }

    /**
     * Sumas de hits (completed/expected) para varios clientIds en el rango [from, to] usando la zona indicada.
     * Devuelve un PatrolRunRepo.HitsSum (valores 0 si no hay resultados).
     */
    PatrolRunRepo.HitsSum hitsSum(Collection<Long> clientIds, LocalDate from, LocalDate to, ZoneId zone);

    /**
     * Conveniencia: hitsSum para un único clientId (envuelve y delega en la variante multi).
     */
    default PatrolRunRepo.HitsSum hitsSum(Long clientId, LocalDate from, LocalDate to, ZoneId zone) {
        return hitsSum(clientId == null ? null : java.util.List.of(clientId), from, to, zone);
    }

        /**
     * Devuelve los conteos horarios de patrols por site (siteId, siteName, hour, count)
     * para los clientIds indicados en la fecha dada (se usa tz para bucketizar horas).
     * - clientIds: colección de clientes a incluir (no llamar con lista vacía)
     * - date: día a agregar (se interpreta startOfDay(date,tz) .. startOfDay(date+1,tz))
     * - tz: id de zona (ej. "America/Santiago"), si es null se usa "UTC"
     */
    List<PatrolHourlyDto> getPatrolHourlyCounts(Collection<Long> clientIds, LocalDate date, String tz);

    /**
     * Conveniencia: variante para un único clientId.
     */
    default List<PatrolHourlyDto> getPatrolHourlyCounts(Long clientId, LocalDate date, String tz) {
        return getPatrolHourlyCounts(clientId == null ? null : java.util.List.of(clientId), date, tz);
    }

    /**
     * Cuenta rondas (patrol runs) cuyos sitios pertenecen a cualquiera de clientIds
     * en la fecha indicada (la zona tz se usa para convertir LocalDate a instantes).
     *
     * @param clientIds lista de clients (si vacía o null devuelve 0)
     * @param date LocalDate del día a contar (según tz)
     * @param tz IANA zone id (p.ej. "America/Santiago"), null -> system default
     * @return cantidad de rondas
     */
    long countByClientIdsAndDate(List<Long> clientIds, LocalDate date, String tz);
    /**
     * Conveniencia: usa tz por defecto (system zone)
     */
    default long countByClientIdsAndDate(List<Long> clientIds, LocalDate date) {
        return countByClientIdsAndDate(clientIds, date, null);
    }

}
