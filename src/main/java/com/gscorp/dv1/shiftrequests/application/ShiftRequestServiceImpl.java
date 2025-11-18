package com.gscorp.dv1.shiftrequests.application;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.gscorp.dv1.clientaccounts.application.ClientAccountService;
import com.gscorp.dv1.clientaccounts.web.dto.ClientAccountDto;
import com.gscorp.dv1.enums.ShiftRequestStatus;
import com.gscorp.dv1.shiftrequests.infrastructure.ShiftRequest;
import com.gscorp.dv1.shiftrequests.infrastructure.ShiftRequestRepository;
import com.gscorp.dv1.shiftrequests.infrastructure.ShiftSchedule;
import com.gscorp.dv1.shiftrequests.web.dto.CreateShiftRequest;
import com.gscorp.dv1.shiftrequests.web.dto.ShiftRequestDto;
import com.gscorp.dv1.sites.application.SiteService;
import com.gscorp.dv1.sites.infrastructure.Site;
import com.gscorp.dv1.sites.infrastructure.SiteRepository;
import com.gscorp.dv1.users.application.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ShiftRequestServiceImpl implements ShiftRequestService {

    private final ShiftRequestRepository shiftRequestRepository;
    private final SiteRepository siteRepository;
    private final UserService userService;
    private final ClientAccountService clientAccountService;
    private final SiteService siteService;

    @Override
    public List<ShiftRequestDto> findAll() {
        List<ShiftRequest> shiftRequests = shiftRequestRepository.findAll();
        return shiftRequests.stream()
            .map(ShiftRequestDto::fromEntity)
            .toList();
    }

    @Override
    public Optional<ShiftRequestDto> findById(Long id) {
        return shiftRequestRepository.findById(id)
            .map(ShiftRequestDto::fromEntity);
    }

    @Override
    public Optional<ShiftRequestDto> update(Long id, CreateShiftRequest req) {
        ShiftRequest shiftRequest = shiftRequestRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("ShiftRequest not found"));

        // Mapear los campos del request a la entidad
        Site siteEntity = (req.siteId() != null) 
            ? siteRepository.findById(req.siteId())
                .orElseThrow(() -> new IllegalArgumentException("Site not found"))
            : null ;
        shiftRequest.setSite(siteEntity);

        if(req.type() != null) {
            shiftRequest.setType(req.type());
        }

        shiftRequest.setClientAccountId(req.accountId());
        shiftRequest.setStartDate(req.startDate());
        shiftRequest.setEndDate(req.endDate());
        shiftRequest.setDescription(req.description());

        //Definir status
        shiftRequest.setStatus(ShiftRequestStatus.REQUESTED);

        ShiftRequest saved = shiftRequestRepository.save(shiftRequest);
        return Optional.ofNullable(ShiftRequestDto.fromEntity(saved));
    }


    @Override
    @Transactional(readOnly = true)
    public List<ShiftRequestDto> findByClientIds(Collection<Long> clientIds) {
        if (clientIds == null || clientIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<ShiftRequest> entities = shiftRequestRepository.findBySiteClientIdInFetchSiteAndSchedules(clientIds);
        return entities.stream()
                .map(ShiftRequestDto::fromEntity)
                .collect(Collectors.toList());
    }


    @Override
    @Transactional(readOnly = true)
    public List<ShiftRequestDto> findShiftRequestDtosForPrincipal(Authentication authentication) {

        if (authentication == null ){
            return Collections.emptyList();
        }

        Long userId = userService.getUserIdFromAuthentication(authentication);
        if (userId == null) {
            return Collections.emptyList();
        }    

        boolean isAdmin = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .anyMatch("ROLE_ADMINISTRATOR"::equals);

        if (isAdmin) {
            // Método del repo que devuelve todas las entidades con site y schedules inicializados (query con DISTINCT)
            List<ShiftRequest> entities = shiftRequestRepository.findAllWithSiteAndSchedules();
            return entities.stream()
                    .map(ShiftRequestDto::fromEntity)
                    .collect(Collectors.toList());
        }

        List<Long> clientIds = userService.getClientIdsForUser(userId);
        if (clientIds == null || clientIds.isEmpty()) {
            return Collections.emptyList();
        }

        return findByClientIds(clientIds);
    }


    /**
     * Creación principal con validación de permisos del usuario.
     */
    @Override
    @Transactional
    public ShiftRequestDto createShiftRequest(CreateShiftRequest req, Long userId) {
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado");
        }

        List<Long> allowedClientIds = userService.getClientIdsForUser(userId);
        if (allowedClientIds == null || allowedClientIds.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No autorizado para crear solicitudes de turno");
        }

        // Validaciones básicas de request
        if (req.type() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tipo de solicitud es obligatorio");
        }
        if (req.startDate() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Fecha de inicio es obligatoria");
        }
        // si endDate es null, lo normalizamos a startDate
        LocalDate start = req.startDate();
        LocalDate end = req.endDate() != null ? req.endDate() : start;
        if (end.isBefore(start)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La fecha de fin no puede ser anterior a la fecha de inicio");
        }

        // Obtener clientId asociado al site
        Long siteId = req.siteId();
        if (siteId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "siteId es obligatorio");
        }

        Long siteClientId = siteService.getClientIdForSite(siteId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Site no encontrado"));

        if (!allowedClientIds.contains(siteClientId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No autorizado para usar ese sitio");
        }

        // cargar la entidad Site
        Site site = siteService.findById(siteId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Site no encontrado"));

        // validar accountId si se envía
        Long accountId = req.accountId();
        if (accountId != null) {
            ClientAccountDto acctDto = clientAccountService.getAccountDtoIfOwned(accountId, userId);
            if (acctDto == null) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cuenta no autorizada");
            }
            Long acctClientId = acctDto.clientId();
            if (acctClientId != null && !acctClientId.equals(siteClientId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La cuenta de cliente no pertenece al mismo cliente que el sitio");
            }
        }

        // Delegar a helper que hace la persistencia con reintentos por colisiones de código
        ShiftRequest saved = buildAndSaveShiftRequestWithRetries(req, site, start, end);

        ShiftRequest enriched = shiftRequestRepository.findByIdWithSiteAndSchedules(saved.getId())
                .orElse(saved);

        return ShiftRequestDto.fromEntity(enriched);
    }


    @Override
    @Transactional
    public ShiftRequestDto createShiftRequestForPrincipal(CreateShiftRequest req, Authentication authentication) {
        Long userId = userService.getUserIdFromAuthentication(authentication);
        return createShiftRequest(req, userId);
    }


    @Override
    @Transactional(readOnly = true)
    public ShiftRequestDto getDtoIfOwned(Long shiftRequestId, Long userId) {
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado");
        }

        List<Long> allowedClientIds = userService.getClientIdsForUser(userId);
        if (allowedClientIds == null || allowedClientIds.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No autorizado para ver esta solicitud");
        }

        // cargar entidad básica
        ShiftRequest sr = shiftRequestRepository.findById(shiftRequestId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Solicitud no encontrada"));

        // obtener siteId (si existe)
        Long siteId = sr.getSite() != null ? sr.getSite().getId() : null;
        Long siteClientId = null;
        if (siteId != null) {
            // usa el service que devuelve el clientId del site (recomendado)
            siteClientId = siteService.getClientIdForSite(siteId).orElse(null);
        }

        if (siteClientId == null || !allowedClientIds.contains(siteClientId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No autorizado para ver esta solicitud");
        }

        // intentar recargar la entidad con site y schedules (método que tienes en el repo)
        ShiftRequest enriched = shiftRequestRepository.findByIdWithSiteAndSchedules(shiftRequestId)
                .orElse(sr);

        // mapear a DTO usando el fromEntity que ya existe
        return ShiftRequestDto.fromEntity(enriched);
    }

     /**
     * Construye y persiste la entidad ShiftRequest (incluye mapeo de schedules).
     * No realiza validaciones de permisos; se asume que el caller ya las hizo si aplica.
     */
    private ShiftRequest buildAndSaveShiftRequest(CreateShiftRequest req, Site site, LocalDate start, LocalDate end) {
        // Determinar prefijo según el enum RequestType
        String prefix;
        switch (req.type()) {
            case FIXED -> prefix = "TF";
            case ESPORADIC -> prefix = "TS";
            default -> prefix = "TR";
        }

        // intentar inferir next code por site+prefix (usa repo query)
        String lastCode = shiftRequestRepository.findLastCodeBySiteIdAndPrefix(site.getId(), prefix);
        int nextCorrelative = 1;
        if (lastCode != null && lastCode.startsWith(prefix)) {
            try {
                nextCorrelative = Integer.parseInt(lastCode.substring(prefix.length())) + 1;
            } catch (NumberFormatException e) {
                nextCorrelative = 1;
            }
        }
        String code = prefix + String.format("%04d", nextCorrelative);

        ShiftRequest entity = ShiftRequest.builder()
                .code(code)
                .site(site)
                .clientAccountId(req.accountId())
                .type(req.type())
                .startDate(start)
                .endDate(end)
                .status(ShiftRequestStatus.REQUESTED)
                .description(req.description())
                .build();

        // mapear schedules si vienen
        if (req.schedules() != null && !req.schedules().isEmpty()) {
            try {
                List<ShiftSchedule> schedules = req.schedules().stream().map(schedReq -> {
                    ShiftSchedule ss = ShiftSchedule.builder()
                            .dayFrom(schedReq.dayFrom())
                            .dayTo(schedReq.dayTo())
                            .startTime(LocalTime.parse(schedReq.startTime()))
                            .endTime(LocalTime.parse(schedReq.endTime()))
                            .shiftRequest(entity)
                            .build();
                    return ss;
                }).toList();
                entity.setSchedules(schedules);
            } catch (DateTimeParseException dtpe) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Formato de hora inválido en schedules: " + dtpe.getMessage(), dtpe);
            }
        }

        return shiftRequestRepository.save(entity);
    }


    /**
     * Persistencia con reintentos en caso de colisión por código único.
     */
    private ShiftRequest buildAndSaveShiftRequestWithRetries(CreateShiftRequest req, Site site, LocalDate start, LocalDate end) {
        final int MAX_ATTEMPTS = 3;
        ShiftRequest saved = null;
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                saved = buildAndSaveShiftRequest(req, site, start, end);
                return saved;
            } catch (DataIntegrityViolationException dive) {
                if (attempt == MAX_ATTEMPTS) {
                    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "No se pudo generar un código único para la solicitud de turno", dive);
                }
                // else continuar y reintentar (el helper volverá a calcular next code)
            }
        }
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "No se pudo crear la solicitud de turno");
    }


}
