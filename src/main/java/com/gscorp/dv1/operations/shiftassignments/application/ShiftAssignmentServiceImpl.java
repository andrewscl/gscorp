package com.gscorp.dv1.operations.shiftassignments.application;

import java.time.OffsetDateTime;
import java.time.ZoneId;
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

import com.gscorp.dv1.components.ZoneResolver;
import com.gscorp.dv1.components.dto.ZoneResolutionResult;
import com.gscorp.dv1.enums.ShiftAssignmentStatus;
import com.gscorp.dv1.hr.employees.infrastructure.Employee;
import com.gscorp.dv1.hr.employees.infrastructure.EmployeeRepository;
import com.gscorp.dv1.operations.shiftassignments.infrastructure.ShiftAssignment;
import com.gscorp.dv1.operations.shiftassignments.infrastructure.ShiftAssignmentRepository;
import com.gscorp.dv1.operations.shiftassignments.infrastructure.projections.ShiftAssignmentProjection;
import com.gscorp.dv1.operations.shiftassignments.web.dto.CreateShiftAssignmentRequest;
import com.gscorp.dv1.operations.shiftassignments.web.dto.ShiftAssignmentDto;
import com.gscorp.dv1.operations.shiftrequests.infrastructure.ShiftRequest;
import com.gscorp.dv1.operations.shiftrequests.infrastructure.ShiftRequestRepository;
import com.gscorp.dv1.operations.shiftrequests.infrastructure.ShiftRequestScheduleRepository;
import com.gscorp.dv1.operations.shiftrequests.infrastructure.projections.ShiftRequestScheduleProjection;
import com.gscorp.dv1.operations.shiftrequests.web.dto.ShiftRequestScheduleDto;
import com.gscorp.dv1.users.application.UserScopeService;
import com.gscorp.dv1.users.application.dto.ProjectScope;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShiftAssignmentServiceImpl implements ShiftAssignmentService{

    private final ShiftAssignmentRepository shiftAssignmentRepository;
    private final ShiftRequestRepository shiftRequestRepository;
    private final ShiftRequestScheduleRepository shiftRequestScheduleRepository;
    private final EmployeeRepository employeeRepository;
    private final ZoneResolver zoneResolver;
    private final ShiftAssignmentProcessor shiftAssignmentProcessor;
    private final UserScopeService userScopeService;

    @Transactional(readOnly = true)
    public Page<ShiftAssignmentDto> getShiftAssignmentsList(
            UUID userExternalId,
            UUID siteExternalId,
            ShiftAssignmentStatus status,
            int page,
            int size,
            String requestedZone
    ){
        if (userExternalId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado");
        }
        ProjectScope scope = userScopeService.getProjectScope();
        ZoneResolutionResult zoneResult = zoneResolver.resolveZone(userExternalId, requestedZone);
        ZoneId targetZone = zoneResult.zoneId();
        int safePage = Math.max(0, page);
        int safeSize = Math.min(Math.max(5, size), 200);
        PageRequest pageable = PageRequest.of(safePage, safeSize);        

        Page<ShiftAssignmentProjection> projections =
            shiftAssignmentRepository.findPageByProjectIds(
                scope.ignoreFilter(), scope.projectIds(), siteExternalId, status, pageable);

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
                            schedulesByRequestId.getOrDefault(p.getShiftRequestId(), List.of()),
                            targetZone
                            ));
    }

    @Transactional
    public ShiftAssignmentDto createShiftAssignment (
            UUID userExternalId,
            String requestedZone,
            CreateShiftAssignmentRequest request
    ){
        if (userExternalId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado");
        }
        ZoneResolutionResult zoneResult = zoneResolver.resolveZone(userExternalId, requestedZone);
        ZoneId targetZone = zoneResult.zoneId();
        ShiftRequest shiftRequest =
            shiftRequestRepository.findByExternalId(request.shiftRequestExternalId())
                .orElseThrow(() -> new EntityNotFoundException("Requerimiento de turno no encontrado")) ;
        Employee employee =
            employeeRepository.findByExternalId(request.employeeExternalId())
                .orElseThrow(() -> new EntityNotFoundException("Empleado no encontrado")) ;
        OffsetDateTime normalizedAssignedAt = request.assignedAt().toLocalDate()
                                                .atStartOfDay(targetZone).toOffsetDateTime();
        OffsetDateTime normalizedAssignedUntil = (request.assignedUntil() != null)
            ? request.assignedUntil().toLocalDate().atStartOfDay(targetZone).toOffsetDateTime()
            : null ;
        ShiftAssignment shiftAssignment = ShiftAssignment.builder()
                    .employee(employee)
                    .shiftRequest(shiftRequest)
                    .notes(request.notes())
                    .status(ShiftAssignmentStatus.ASSIGNED)
                    .assignedAt(normalizedAssignedAt)
                    .assignedUntil(normalizedAssignedUntil)
                    .startCycleNumber(request.startCycleNumber())
                    .build();
        ShiftAssignment savedAssignment = shiftAssignmentRepository.save(shiftAssignment);
        shiftAssignmentProcessor.processShiftsForAssignment(savedAssignment, targetZone);
        return ShiftAssignmentDto.fromEntity(savedAssignment, targetZone);
    }

    @Transactional(readOnly = true)
    public ShiftAssignmentDto getByExternalId(
        UUID userExternalId,
        UUID shiftAssignmentExternalId,
        String requestedZone
    ){
        if (shiftAssignmentExternalId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado");
        }
        ZoneResolutionResult zoneResult = zoneResolver.resolveZone(userExternalId, requestedZone);
        ZoneId targetZone = zoneResult.zoneId();
        return shiftAssignmentRepository.findByExternalId(shiftAssignmentExternalId)
                .map(entity -> ShiftAssignmentDto.fromEntity(entity, targetZone))
                .orElseThrow(() -> new EntityNotFoundException("Asignación no encontrada"));
    }

}
