package com.gscorp.dv1.operations.shiftrequests.application;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;

import com.gscorp.dv1.enums.ShiftRequestType;
import com.gscorp.dv1.operations.shiftrequests.web.dto.CreateShiftRequest;
import com.gscorp.dv1.operations.shiftrequests.web.dto.ShiftRequestDto;
import com.gscorp.dv1.operations.shiftrequests.web.dto.ShiftRequestDtoWithSchedules;
import com.gscorp.dv1.operations.shiftrequests.web.dto.UpdateShiftRequestDto;

public interface ShiftRequestService {

    List<ShiftRequestDtoWithSchedules> findAll();

    Optional<ShiftRequestDtoWithSchedules> findById(Long id);

    ShiftRequestDtoWithSchedules update(
                UUID externalId, 
                UpdateShiftRequestDto req);

    /**
     * Devuelve ShiftRequestDto filtrados por clientIds (los DTOs deben contener
     * los campos necesarios para la tabla).
     */
    List<ShiftRequestDtoWithSchedules> findByClientIds(Collection<Long> clientIds);

        boolean deleteShiftRequest(Long Id);

    /**
     * Crea un ShiftRequest validando que el site (y opcionalmente clientAccountId)
     * pertenezcan a uno de los clients del usuario (userId).
     */
    ShiftRequestDtoWithSchedules createShiftRequest(CreateShiftRequest req, UUID userEmployeeId);

    /**
     * Conveniencia: resuelve userId desde Authentication y delega.
     */
    ShiftRequestDtoWithSchedules createShiftRequestForPrincipal(CreateShiftRequest req, Authentication authentication);


    ShiftRequestDtoWithSchedules getAllowedShiftRequestByExternalId(
                                UUID userExternalId,
                                UUID shiftRequestExternalId);


    List<ShiftRequestDto> findByUserIdAndDateBetween(
            UUID userExternalId,
            LocalDate fromDate,
            LocalDate toDate,
            String clientTz,
            Long siteId,
            ShiftRequestType type
    );

        Page<ShiftRequestDto> getShiftRequestsTable(
                    UUID userExternalId,
                    ZoneId zoneId,
                    LocalDate fromDate,
                    LocalDate toDate,
                    Long siteId,
                    Long projectId,
                    ShiftRequestType type,
                    int page,
                    int size
        );

}
