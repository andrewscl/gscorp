package com.gscorp.dv1.shiftrequests.application;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.security.core.Authentication;

import com.gscorp.dv1.shiftrequests.web.dto.CreateShiftRequestRequest;
import com.gscorp.dv1.shiftrequests.web.dto.ShiftRequestDto;

public interface ShiftRequestService {
    List<ShiftRequestDto> findAll();
    Optional<ShiftRequestDto> findById(Long id);
    ShiftRequestDto create(CreateShiftRequestRequest shiftRequest);
    Optional<ShiftRequestDto> update(Long id, 
            CreateShiftRequestRequest createShiftRequestDto);

    /**
     * Devuelve ShiftRequestDto filtrados por clientIds (los DTOs deben contener
     * los campos necesarios para la tabla).
     */
    List<ShiftRequestDto> findByClientIds(Collection<Long> clientIds);

    /**
     * Resuelve el userId desde Authentication y devuelve los ShiftRequest visibles
     * para ese principal (vacío si no autenticado / sin clients).
     */
    List<ShiftRequestDto> findShiftRequestDtosForPrincipal(Authentication authentication);

    /**
     * Crea un ShiftRequest validando que el site (y opcionalmente clientAccountId)
     * pertenezcan a uno de los clients del usuario (userId).
     */
    ShiftRequestDto createShiftRequest(CreateShiftRequestRequest req, Long userId);

    /**
     * Conveniencia: resuelve userId desde Authentication y delega.
     */
    ShiftRequestDto createShiftRequestForPrincipal(CreateShiftRequestRequest req, Authentication authentication);

    /**
     * Devuelve el DTO si existe y pertenece a alguno de los clients del usuario.
     */
    ShiftRequestDto getDtoIfOwned(Long shiftRequestId, Long userId);


}
