package com.gscorp.dv1.forecast.application;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gscorp.dv1.clients.application.ClientService;
import com.gscorp.dv1.clients.web.dto.ClientBriefDto;
import com.gscorp.dv1.components.ZoneResolver;
import com.gscorp.dv1.components.dto.ZoneResolutionResult;
import com.gscorp.dv1.enums.Periodicity;
import com.gscorp.dv1.forecast.infrastructure.Forecast;
import com.gscorp.dv1.forecast.infrastructure.ForecastRepository;
import com.gscorp.dv1.forecast.web.dto.ForecastCreateDto;
import com.gscorp.dv1.forecast.web.dto.ForecastFormPayload;
import com.gscorp.dv1.forecast.web.dto.ForecastFormPrefill;
import com.gscorp.dv1.forecast.web.dto.ForecastPointDto;
import com.gscorp.dv1.forecast.web.dto.ForecastRecordDto;
import com.gscorp.dv1.forecast.web.dto.ForecastTableRowDto;
import com.gscorp.dv1.projects.application.ProjectService;
import com.gscorp.dv1.sites.application.SiteService;
import com.gscorp.dv1.users.application.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ForecastServiceImpl implements ForecastService{


    private final UserService userService;
    private final ForecastRepository forecastRepo;
    private final ClientService clientService;
    private final ProjectService projectService;
    private final SiteService siteService;
    private final ZoneResolver zoneResolver;


    @Override
    @Transactional(readOnly = true)
    public List<ForecastPointDto> getForecastSeriesForUserByDates(Long userId, LocalDate fromDate, LocalDate toDate, ZoneId zone){

        List<Long> clientIds = userService.getClientIdsForUser(userId);
        if (clientIds == null || clientIds.isEmpty()) {
            // devolver zeros para el rango pedido
            List<ForecastPointDto> empty = new ArrayList<>();
            LocalDate d = fromDate;
            while (!d.isAfter(toDate)) {
                empty.add(new ForecastPointDto(d.toString(), BigDecimal.ZERO));
                d = d.plusDays(1);
            }
            return empty;
        }

        // 2) convertir a OffsetDateTime semi-abierto [fromDate, toDate)
        OffsetDateTime fromOffset = fromDate.atStartOfDay(zone).toOffsetDateTime();
        OffsetDateTime toOffset = toDate.plusDays(1).atStartOfDay(zone).toOffsetDateTime();

        // 3) traer forecasts (repo debe devolver entidades Forecast)
        List<Forecast> forecasts = forecastRepo.findByClientIdsAndDateBetween(clientIds, fromOffset, toOffset);
        if (forecasts == null || forecasts.isEmpty()) {
            List<ForecastPointDto> empty = new ArrayList<>();
            LocalDate d = fromDate;
            while (!d.isAfter(toDate)) {
                empty.add(new ForecastPointDto(d.toString(), BigDecimal.ZERO));
                d = d.plusDays(1);
            }
            return empty;
        }

        // 4) Agrupar por fecha local (periodStart). Como la entidad usa LocalDate para periodStart,
        //    no necesitamos conversión por zone; si tu entidad usara OffsetDateTime, conviértelo aquí.
        Map<LocalDate, BigDecimal> grouped = forecasts.stream()
            .filter(f -> f.getPeriodStart() != null && f.getValue() != null)
            .collect(Collectors.groupingBy(
                    f -> f.getPeriodStart().atZoneSameInstant(zone).toLocalDate(), // <-- conversión aquí
                    Collectors.mapping(Forecast::getValue,
                            Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
            ));

        // 5) construir serie final (un punto por día del rango, con suma de values o 0)
        List<ForecastPointDto> series = new ArrayList<>();
        LocalDate d = fromDate;
        while (!d.isAfter(toDate)) {
            BigDecimal v = grouped.getOrDefault(d, BigDecimal.ZERO);
            series.add(new ForecastPointDto(d.toString(), v));
            d = d.plusDays(1);
        }

        return series;

    }


    @Override
    @Transactional(readOnly = true)
    public List<ForecastTableRowDto> loadTableRowForUserAndDates(
                            Long userId, LocalDate fromDate, LocalDate toDate, ZoneId zone) {
            Objects.requireNonNull(userId, "userId es requerido");
            Objects.requireNonNull(fromDate, "fromDate es requerido");
            Objects.requireNonNull(toDate, "toDate es requerido");
            Objects.requireNonNull(zone, "zone es requerido");

            List<Long> clientIds = userService.getClientIdsForUser(userId);
            if (clientIds == null || clientIds.isEmpty()) {
                return Collections.emptyList();
            }

            // convertir a OffsetDateTime semi-abierto [fromDate, toDate)
            OffsetDateTime fromOffset = zoneResolver.toStartOfDay(fromDate, zone);
            OffsetDateTime toOffset = zoneResolver.toEndOfDayInclusive(toDate, zone);

            List<ForecastTableRowDto> rows = forecastRepo.findRowsForClientIdsAndDates(
                                                    clientIds, fromOffset, toOffset);

            return rows == null ? Collections.emptyList() : rows;

    }


    @Override
    @Transactional(readOnly = true)
    public ForecastFormPayload prepareCreateForecastForm(Long userId) {

        if (userId == null) {
            // política: devolver payload vacío (el controller puede redireccionar a login)
            ForecastFormPrefill emptyPrefill = new ForecastFormPrefill(
                null, null, null,
                null,    // forecastMetric
                null, // periodicity por defecto para la UI
                null,    // periodStart (podrías usar today si prefieres)
                null,
                null, null,
                BigDecimal.ZERO,
                null, null,
                null,
                1,
                ZoneId.systemDefault().getId()
            );
            return new ForecastFormPayload(emptyPrefill, Collections.emptyList());
        }

        List<ClientBriefDto> clients = Collections.emptyList();
        try {
            clients = clientService.getBriefByUserId(userId);
            if(clients == null) clients = Collections.emptyList();
        } catch (Exception ex) {
            // loguear y continuar con lista vacía
            log.error("Error al obtener clients para userId {}: {}", userId, ex.getMessage(), ex);
            clients = Collections.emptyList();
        }

        ZoneId zoneId = ZoneId.systemDefault();

        LocalDate today = LocalDate.now(zoneId);
        LocalDate defaultStart = today;
        LocalDate defaultEnd = today.plusDays(1);

        ForecastFormPrefill prefill = new ForecastFormPrefill(
            null,
            null,
            null,
            null,
            null,
            defaultStart,
            defaultEnd,
            null,
            null,
            BigDecimal.ZERO,
            null,
            null,
            null,
            null,
            zoneId.getId()

        );

        return new ForecastFormPayload(prefill, clients);
    }


    @Override
    public ForecastRecordDto createForecast (ForecastCreateDto req, Long userId) {

        // 1) Validaciones de negocio básicas (existen client/project/site, permisos, etc.)
        if (req.clientId() == null) {
            throw new IllegalArgumentException("clientId is required");
        }
        if (clientService.findById(req.clientId()) == null) {
            throw new IllegalArgumentException("client not found: " + req.clientId());
        }
        if (req.projectId() != null && projectService.findById(req.projectId()) == null) {
            throw new IllegalArgumentException("project not found: " + req.projectId());
        }
        if (req.siteId() != null && siteService.findById(req.siteId()) == null) {
            throw new IllegalArgumentException("site not found: " + req.siteId());
        }

        // resolver zona usando ZoneResolver y el userId que recibe el método
        ZoneResolutionResult zr = zoneResolver.resolveZone(userId, req.tz());
        ZoneId zone = zr.zoneId();


        // Validaciones y construcción de periodStart / periodEnd
        OffsetDateTime periodStart;
        if (req.periodicity() == Periodicity.HOURLY) {
            if (req.periodStartHour() == null) {
                throw new IllegalArgumentException("periodStartHour is required for HOURLY periodicity");
            }
            if (req.periodStartHour() < 0 || req.periodStartHour() > 23) {
                throw new IllegalArgumentException("periodStartHour must be 0..23");
            }
            LocalDateTime ldt = req.periodStart().atTime(req.periodStartHour(), 0);
            periodStart = ldt.atZone(zone).toOffsetDateTime();
        } else {
            periodStart = zoneResolver.toStartOfDay(req.periodStart(), zone);
        }

        OffsetDateTime periodEnd = null;
        if (req.periodEnd() != null) {
            if (req.periodicity() == Periodicity.HOURLY) {
                if (req.periodEndHour() == null) {
                    throw new IllegalArgumentException("periodEndHour is required for HOURLY periodicity");
                }
                if (req.periodEndHour() < 0 || req.periodEndHour() > 23) {
                    throw new IllegalArgumentException("periodEndHour must be 0..23");
                }
                LocalDateTime ldt = req.periodEnd().atTime(req.periodEndHour(), 0);
                periodEnd = ldt.atZone(zone).toOffsetDateTime();
            } else {
                // si prefieres que sea el final del día usa toEndOfDayInclusive
                periodEnd = zoneResolver.toStartOfDay(req.periodEnd(), zone);
            }
        }


        // 3) Construir entidad Forecast
        Forecast f = new Forecast();
        f.setClientId(req.clientId());
        f.setProjectId(req.projectId());
        f.setSiteId(req.siteId());
        // forecastMetric en tu DTO ya es ForecastMetric enum; si no, conviértelo
        f.setForecastMetric(req.forecastMetric());
        f.setPeriodicity(req.periodicity());
        f.setPeriodStart(periodStart);
        f.setPeriodEnd(periodEnd);
        f.setValue(req.value());
        f.setUnits(req.units());
        f.setConfidence(req.confidence());
        f.setNote(req.note());
        f.setForecastVersion(req.forecastVersion());
        f.setCreatedBy(userId);
        f.setIsActive(Boolean.TRUE);

        // 4) Persistir
        Forecast saved = forecastRepo.save(f);

        // 5) Mapear a ForecastRecordDto usando el heplper fromEntity
        ForecastRecordDto result = ForecastRecordDto.fromEntity(saved);

        return result;

        
    }
}
