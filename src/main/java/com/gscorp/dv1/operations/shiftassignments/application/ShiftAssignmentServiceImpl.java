package com.gscorp.dv1.operations.shiftassignments.application;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.gscorp.dv1.admin.clients.application.ClientService;
import com.gscorp.dv1.enums.ShiftAssignmentStatus;
import com.gscorp.dv1.operations.shiftassignments.infrastructure.ShiftAssignmentRepository;
import com.gscorp.dv1.operations.shiftassignments.infrastructure.projections.ShiftAssignmentProjection;
import com.gscorp.dv1.operations.shiftassignments.web.dto.CreateShiftAssignmentRequest;
import com.gscorp.dv1.operations.shiftassignments.web.dto.ShiftAssignmentDto;
import com.gscorp.dv1.operations.shiftrequests.infrastructure.ShiftRequestScheduleRepository;
import com.gscorp.dv1.operations.shiftrequests.infrastructure.projections.ShiftRequestScheduleProjection;
import com.gscorp.dv1.operations.shiftrequests.web.dto.ShiftRequestScheduleDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShiftAssignmentServiceImpl implements ShiftAssignmentService{

    private final ClientService clientService;
    private final ShiftAssignmentRepository shiftAssignmentRepository;
    private final ShiftRequestScheduleRepository shiftRequestScheduleRepository;

    @Transactional(readOnly = true)
    public Page<ShiftAssignmentDto> getShiftAssignmentList(
            UUID userExternalId,
            ShiftAssignmentStatus status,
            int page,
            int size
    ){
        List<Long> clientIds = clientService.getClientIdsByUserExternalId(userExternalId);
        if (clientIds == null || clientIds.isEmpty()) {
            log.debug("No clientIds for user {} -> returning zero series for {}..{}", userExternalId);
            return Page.empty();
        }
        int safePage = Math.max(0, page);
        int safeSize = Math.min(Math.max(5, size), 200);
        PageRequest pageable = PageRequest.of(safePage, safeSize);        

        Page<ShiftAssignmentProjection> projections =
                shiftAssignmentRepository.findByClientIds(
                    clientIds, status, pageable);
        if (projections.isEmpty()) return Page.empty();
        List<Long> shiftRequestIds = projections.getContent().stream()
                    .map(p -> p.getShiftRequestId())
                    .filter(id -> id != null)
                    .distinct()
                    .toList();
        List<ShiftRequestScheduleProjection> shiftRequestScheduleProjections =
                    shiftRequestScheduleRepository.findByShiftRequestIds(shiftRequestIds);
        Map<Long, List<ShiftRequestScheduleDto>> schedulesByRequestId =
                    shiftRequestScheduleProjections
                            .stream()
                            .collect(Collectors.groupingBy(
                            sp -> sp.getShiftRequestId(),
                            Collectors.mapping(
                                sp -> ShiftRequestScheduleDto.fromProjection(sp),
                                Collectors.toList()
                            )   
                        ));
        return projections.map(
                    p -> ShiftAssignmentDto.fromProjection(
                            p,
                            schedulesByRequestId.getOrDefault(p.getShiftRequestId(), List.of())
                            ));
    }

    @Transactional
    public ShiftAssignmentDto createShiftAssignment (
            UUID userExternalId,
            CreateShiftAssignmentRequest request
    ){
        if (userExternalId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado");
        }

        return ShiftAssignmentDto.fromProjection(null, null);
    }

}
