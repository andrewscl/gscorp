package com.gscorp.dv1.shiftrequests.application;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.core.Authentication;

import com.gscorp.dv1.enums.ShiftRequestType;
import com.gscorp.dv1.shiftrequests.web.dto.CreateShiftRequest;
import com.gscorp.dv1.shiftrequests.web.dto.ShiftRequestDto;
import com.gscorp.dv1.shiftrequests.web.dto.ShiftRequestDtoLight;

public interface ShiftRequestService {
    List<ShiftRequestDto> findAll();
    Optional<ShiftRequestDto> findById(Long id);
    Optional<ShiftRequestDto> update(Long id, 
            CreateShiftRequest createShiftRequestDto);

    /**
     * Devuelve ShiftRequestDto filtrados por clientIds (los DTOs deben contener
     * los campos necesarios para la tabla).
     */
    List<ShiftRequestDto> findByClientIds(Collection<Long> clientIds);

    /**
     * Crea un ShiftRequest validando que el site (y opcionalmente clientAccountId)
     * pertenezcan a uno de los clients del usuario (userId).
     */
    ShiftRequestDto createShiftRequest(CreateShiftRequest req, UUID userEmployeeId);

    /**
     * Conveniencia: resuelve userId desde Authentication y delega.
     */
    ShiftRequestDto createShiftRequestForPrincipal(CreateShiftRequest req, Authentication authentication);

    /**
     * Devuelve el DTO si existe y pertenece a alguno de los clients del usuario.
     */
    ShiftRequestDto getDtoIfOwned(Long shiftRequestId, UUID userExternalId);


    List<ShiftRequestDtoLight> findByUserIdAndDateBetween(
            UUID userExternalId,
            LocalDate fromDate,
            LocalDate toDate,
            String clientTz,
            Long siteId,
            ShiftRequestType type
    );

    public boolean deleteShiftRequest(Long Id);


}
