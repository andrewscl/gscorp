package com.gscorp.dv1.shiftrequests.application;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.gscorp.dv1.clientaccounts.application.ClientAccountService;
import com.gscorp.dv1.clientaccounts.web.dto.ClientAccountDto;
import com.gscorp.dv1.enums.RequestType;
import com.gscorp.dv1.enums.ShiftRequestStatus;
import com.gscorp.dv1.shiftrequests.infrastructure.ShiftRequest;
import com.gscorp.dv1.shiftrequests.infrastructure.ShiftRequestRepository;
import com.gscorp.dv1.shiftrequests.infrastructure.ShiftSchedule;
import com.gscorp.dv1.shiftrequests.web.dto.CreateShiftRequestRequest;
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
    @Transactional
    public ShiftRequestDto create(CreateShiftRequestRequest req) {
        // Validaciones básicas
        if (req.type() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tipo de solicitud es obligatorio");
        }
        if (req.siteId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "siteId es obligatorio");
        }

        RequestType requestType = req.type(); // ya es enum
        Long siteId = req.siteId();

        Site site = siteRepository.findById(siteId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Site not found"));

        // Determinar prefijo según el enum RequestType
        String prefix;
        switch (requestType) {
            case FIXED -> prefix = "TF";
            case ESPORADIC -> prefix = "TS";
            default -> prefix = "TX";
        }

        // Buscar el último código con ese prefijo
        String lastCode = shiftRequestRepository.findLastCodeBySiteIdAndPrefix(siteId, prefix);

        int nextCorrelative = 1;
        if (lastCode != null && lastCode.startsWith(prefix)) {
            try {
                nextCorrelative = Integer.parseInt(lastCode.substring(prefix.length())) + 1;
            } catch (NumberFormatException e) {
                nextCorrelative = 1;
            }
        }

        String newCode = prefix + String.format("%04d", nextCorrelative);

        // Construir entidad
        ShiftRequest shiftRequest = ShiftRequest.builder()
                .code(newCode)
                .site(site)
                .type(requestType)
                .clientAccountId(req.accountId())
                .startDate(req.startDate())
                .endDate(req.endDate())
                .status(ShiftRequestStatus.REQUESTED)
                .description(req.description())
                .build();

        // Mapear schedules (validando formatos de hora si aplica)
        if (req.schedules() != null && !req.schedules().isEmpty()) {
            try {
                List<ShiftSchedule> schedules = req.schedules().stream()
                    .map(schedReq -> ShiftSchedule.builder()
                        .dayFrom(schedReq.dayFrom())
                        .dayTo(schedReq.dayTo())
                        .startTime(LocalTime.parse(schedReq.startTime()))
                        .endTime(LocalTime.parse(schedReq.endTime()))
                        .lunchTime(schedReq.lunchTime() != null ? LocalTime.parse(schedReq.lunchTime()) : null)
                        .shiftRequest(shiftRequest)
                        .build()
                    ).toList();
                shiftRequest.setSchedules(schedules);
            } catch (DateTimeParseException dtpe) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Formato de hora inválido en schedules: " + dtpe.getMessage(), dtpe);
            }
        }

        ShiftRequest saved = shiftRequestRepository.save(shiftRequest);

        // recargar con relaciones para evitar lazy issues y mapear a DTO
        ShiftRequest enriched = shiftRequestRepository.findByIdWithSiteAndSchedules(saved.getId())
                .orElse(saved);

        return ShiftRequestDto.fromEntity(enriched);
    }

    @Override
    public Optional<ShiftRequestDto> update(Long id, CreateShiftRequestRequest req) {
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
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShiftRequestDto> findShiftRequestDtosForPrincipal(Authentication authentication) {
        Long userId = userService.getUserIdFromAuthentication(authentication);
        if (userId == null) return Collections.emptyList();

        boolean isAdmin = authentication != null &&
                authentication.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMINISTRATOR"));

        if (isAdmin) {
            return shiftRequestRepository.findAllWithSiteAndSchedules()
                    .stream()
                    .map(ShiftRequestDto::fromEntity)
                    .toList();
        }

        List<Long> clientIds = userService.getClientIdsForUser(userId);
        if (clientIds == null || clientIds.isEmpty()) return Collections.emptyList();

        return findByClientIds(clientIds);
    }


    @Override
    @Transactional
    public ShiftRequestDto createShiftRequest(CreateShiftRequestRequest req, Long userId) {
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
        if (req.startDate() == null || req.endDate() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Fecha de inicio y fecha de fin son obligatorias");
        }
        if (req.endDate().isBefore(req.startDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La fecha de fin no puede ser anterior a la fecha de inicio");
        }

        // Obtener clientId asociado al site (más eficiente que traer entidad completa)
        Long siteClientId = siteService.getClientIdForSite(req.siteId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Site no encontrado"));

        if (!allowedClientIds.contains(siteClientId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No autorizado para usar ese sitio");
        }

        // Ahora cargamos la entidad Site (ya que la usaremos para persistir)
        Site site = siteService.findById(req.siteId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Site no encontrado"));

        // Si se envió clientAccountId: validar que pertenece al usuario y, opcionalmente,
        // que la cuenta esté asociada al mismo client que el site.
        Long accountId = req.accountId();
        if (accountId != null) {
            ClientAccountDto acctDto = clientAccountService.getAccountDtoIfOwned(accountId, userId);
            if (acctDto == null) {
                // getAccountDtoIfOwned normalmente lanza, pero por si acaso:
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cuenta no autorizada");
            }
            Long acctClientId = acctDto.clientId();
            if (acctClientId != null && !acctClientId.equals(siteClientId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La cuenta de cliente no pertenece al mismo cliente que el sitio");
            }
        }

        // Generar código secuencial con reintentos en caso de colisión (unicidad)
        final int MAX_ATTEMPTS = 3;
        //DataIntegrityViolationException lastIntegrityEx = null;
        ShiftRequest saved = null;

        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            String code = generateNextCode();

            ShiftRequest entity = ShiftRequest.builder()
                    .code(code)
                    .site(site)
                    .clientAccountId(accountId)
                    .type(req.type())
                    .startDate(req.startDate())
                    .endDate(req.endDate())
                    .status(ShiftRequestStatus.REQUESTED)
                    .description(req.description())
                    .build();

            try {
                saved = shiftRequestRepository.save(entity);
                // éxito, salimos del loop
                break;
            } catch (DataIntegrityViolationException dive) {
                // posible colisión por code único; guardar y reintentar
                if (attempt == MAX_ATTEMPTS) {
                    throw new ResponseStatusException(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            "No se pudo generar un código único para la solicitud de turno",
                            dive
                    );
                }
                // else continuar al próximo intento
            }
        }

        if (saved == null) {
            // debería haberse lanzado antes, pero por seguridad:
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "No se pudo crear la solicitud de turno");
        }

        // intentar recargar la entidad con site y schedules para evitar LazyInitializationException
        ShiftRequest enriched = shiftRequestRepository.findByIdWithSiteAndSchedules(saved.getId())
                .orElse(saved);

        // devolver DTO mapeado desde la entidad (usa el fromEntity que ya tienes)
        return ShiftRequestDto.fromEntity(enriched);
    }


    @Override
    @Transactional
    public ShiftRequestDto createShiftRequestForPrincipal(CreateShiftRequestRequest req, Authentication authentication) {
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
     * Generación simple de código. Mejora recomendada:
     * - usar tabla de secuencias o consulta 'select max(code)' y parsear, con manejo de concurrencia
     * - o delegar generación a la BD
     */
    private String generateNextCode() {
        long next = shiftRequestRepository.count() + 1;
        // TR001, TR002, TR000123 etc.
        return String.format("TR%03d", next);
    }

}
