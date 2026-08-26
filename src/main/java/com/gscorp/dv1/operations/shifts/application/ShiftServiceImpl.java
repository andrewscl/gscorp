package com.gscorp.dv1.operations.shifts.application;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.gscorp.dv1.components.ZoneResolver;
import com.gscorp.dv1.components.dto.ZoneResolutionResult;
import com.gscorp.dv1.enums.DayOfWeek;
import com.gscorp.dv1.enums.ShiftRequestStatus;
import com.gscorp.dv1.enums.ShiftStatus;
import com.gscorp.dv1.operations.shiftassignments.application.ShiftAssignmentProcessor;
import com.gscorp.dv1.operations.shiftrequests.infrastructure.ShiftRequest;
import com.gscorp.dv1.operations.shiftrequests.infrastructure.ShiftRequestRepository;
import com.gscorp.dv1.operations.shiftrequests.infrastructure.ShiftRequestScheduleRepository;
import com.gscorp.dv1.operations.shiftrequests.infrastructure.projections.ShiftRequestScheduleProjection;
import com.gscorp.dv1.operations.shifts.infrastructure.Shift;
import com.gscorp.dv1.operations.shifts.infrastructure.ShiftRepository;
import com.gscorp.dv1.operations.shifts.infrastructure.projections.ShiftProjection;
import com.gscorp.dv1.operations.shifts.web.dto.ShiftDto;
import com.gscorp.dv1.users.application.UserScopeService;
import com.gscorp.dv1.users.application.dto.ProjectScope;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ShiftServiceImpl implements ShiftService {

    private final ShiftRepository shiftRepository;
    private final ShiftRequestScheduleRepository shiftRequestScheduleRepository;
    private final ShiftRequestRepository shiftRequestRepository;
    private final UserScopeService userScopeService;
    private final ZoneResolver zoneResolver;
    private final ShiftAssignmentProcessor shiftAssignmentProcessor;

    @Transactional(readOnly = true)
    public List<Shift> getShifts(Long siteId, OffsetDateTime from, OffsetDateTime to) {
        return shiftRepository.findBySiteIdAndStartTsBetween(siteId, from, to);
    }

    @Transactional
    public Shift createShift(Shift shift) {
        return shiftRepository.save(shift);
    }

    @Transactional
    public void generateShiftsForNext30days(ShiftRequest shiftRequest, String username, ZoneId zone) {
        LocalDate start = LocalDate.now();
        Optional <Shift> lastGeneratedShift = shiftRepository
            .findFirstByShiftRequestExternalIdOrderByShiftDateDesc(shiftRequest.getExternalId());
        if (lastGeneratedShift.isPresent()) {
            LocalDate lastDate = lastGeneratedShift.get().getShiftDate();
            if (!lastDate.isBefore(start)) {start = lastDate.plusDays(1);}
        }
        LocalDate end = LocalDate.now().plusDays(30);
        if ( start.isAfter(end) ) return;
        List<ShiftRequestScheduleProjection> schedules =
                shiftRequestScheduleRepository.findByShiftRequestId(shiftRequest.getId());
        if(schedules.isEmpty()) return;
        List<Shift> shiftsToSave = new ArrayList<>();
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            for (ShiftRequestScheduleProjection schedule : schedules) {
                if (adheresSchedule(schedule, date)) {
                    // Combinar la fecha del bucle con las horas del horario.
                    LocalDateTime startDateTime = date.atTime(schedule.getStartTime());
                    LocalDateTime endDateTime = date.atTime(schedule.getEndTime());
                    // Gestión de turno nocturnos
                    if (schedule.getEndTime().isBefore(schedule.getStartTime())) {
                        endDateTime = endDateTime.plusDays(1);
                    }
                    OffsetDateTime startOffsetDateTime = startDateTime.atZone(zone).toOffsetDateTime();
                    OffsetDateTime endOffsetDateTime = endDateTime.atZone(zone).toOffsetDateTime();
                    Shift shift = Shift.builder()
                            .site(shiftRequest.getSite())
                            .shiftDate(date)
                            .startTs(startOffsetDateTime)
                            .endTs(endOffsetDateTime)
                            .description(null)
                            .lunchTime(null)
                            .status(ShiftStatus.UNPLANNED)
                            .shiftRequest(shiftRequest)
                            .createdBy(username)
                            .updatedBy(null)
                            .build();
                    shiftsToSave.add(shift);
                }
            }
        }
        if(!shiftsToSave.isEmpty()) shiftRepository.saveAll(shiftsToSave);
    }

    //Helper method to know if adheresSchedule is true for the given date
    private boolean adheresSchedule(ShiftRequestScheduleProjection schedule, LocalDate date) {
        int javaDayOfWeek = date.getDayOfWeek().getValue(); //1=Monday, 7=Sunday
        DayOfWeek dayOfWeek = DayOfWeek.fromDayNumber(javaDayOfWeek);
        DayOfWeek from = schedule.getDayFrom();
        DayOfWeek to = schedule.getDayTo();

        if ( from.getDayNumber() <= to.getDayNumber() ) {
            return dayOfWeek.getDayNumber() >= from.getDayNumber()
            && dayOfWeek.getDayNumber() <= to.getDayNumber();
        }

        //Dia de la semana de inicio es mayor al dia de la semana de termino
        return dayOfWeek.getDayNumber() >= from.getDayNumber()
        || dayOfWeek.getDayNumber() <= to.getDayNumber();

    }

    @Transactional
    public void processApprovedShiftRequests() {
        List<ShiftRequest> approvedRequests =
                        shiftRequestRepository.findAllByStatus(ShiftRequestStatus.APPROVED);
        String systemUsername = "SYSTEM_PROCESS";
        ZoneId fallbackZone = ZoneId.systemDefault();
        for (ShiftRequest request : approvedRequests) {
            ZoneId siteZone = fallbackZone;
            if(request.getSite() != null && request.getSite().getTimeZone() != null) {
                try {
                    siteZone = ZoneId.of(request.getSite().getTimeZone());
                } catch (DateTimeException e) {
                    siteZone = fallbackZone;
                }
            }
            generateShiftsForNext30days(request, systemUsername, siteZone);
            shiftAssignmentProcessor.processAssignmentsForShiftRequest(request, siteZone);
        }
    }

    @Transactional(readOnly = true)
    public Page<ShiftDto> getLastShiftsByShiftRequest(
                            UUID userExternalId,
                            UUID shiftRequestExternalId,
                            int shiftsToShow,
                            String zoneIdStr) { 
        ZoneResolutionResult zoneResult = zoneResolver.resolveZone(
                                                userExternalId, zoneIdStr);
        ZoneId zoneId = zoneResult.zoneId();
        Pageable pageable =
                        PageRequest.of(0, shiftsToShow);
        Page<ShiftProjection> projections =
                    shiftRepository.findLastByShiftRequestExternalId(
                                            shiftRequestExternalId, pageable);
        return projections.map(sp -> ShiftDto.fromProjection(sp, zoneId));
    }

    @Transactional(readOnly = true)
    public List<ShiftDto> getUpcomingByShiftAssignmentExternalId(
                                        UUID userExternalId,
                                        UUID shiftAssignmentExternalId,
                                        Integer shiftsToShow,
                                        String zoneIdStr){
        if (userExternalId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado");
        }
        int limit = (shiftsToShow != null && shiftsToShow > 0) ? shiftsToShow : 10;
        ZoneResolutionResult zoneResult = zoneResolver.resolveZone(
                                                userExternalId, zoneIdStr);
        ZoneId zoneId = zoneResult.zoneId();
        List<ShiftProjection> projections =
            shiftRepository
                .findUpcomingByShiftAssignmentExternalId(
                                        shiftAssignmentExternalId,
                                        PageRequest.of(0, limit));
        return projections.stream()
                    .map(sp -> ShiftDto.fromProjection(sp, zoneId))
                    .toList();
    }

    @Transactional(readOnly = true)
    public Page<ShiftDto> getShiftList(
                        UUID userExternalId,
                        LocalDate startDate,
                        LocalDate endDate,
                        UUID projectExternalId,
                        UUID siteExternalId,
                        UUID shiftRequestExternalId,
                        ShiftStatus status,
                        int page,
                        int size,
                        String zoneIdStr){
        if (userExternalId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado");
        }
        ProjectScope scope = userScopeService.getProjectScope();
        int safePage = Math.max(0, page);
        int safeSize = Math.min(Math.max(5, size), 200);
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.ASC, "startTs"));
        if (scope.hasNoAccess()) return Page.empty(pageable);

        ZoneResolutionResult zoneResult = zoneResolver.resolveZone(
                                                userExternalId, zoneIdStr);
        ZoneId zoneId = zoneResult.zoneId();
        if (startDate == null) startDate = LocalDate.now(zoneId);
        if (endDate == null) endDate = LocalDate.now(zoneId);

        Page<ShiftProjection> projections = shiftRepository
            .findPageByProjectIds(scope.ignoreFilter(), 
                                    scope.projectIds(), 
                                    startDate, 
                                    endDate, 
                                    siteExternalId,
                                    projectExternalId,
                                    shiftRequestExternalId,
                                    status,
                                    pageable);
        return projections.map(sp -> ShiftDto.fromProjection(sp, zoneId));
    }

    @Transactional(readOnly = true)
    public Optional<ShiftDto> getNextUnplannedShift (
                                    ZoneId zoneId,
                                    UUID shiftRequestExternalId,
                                    LocalDate startAssignmentDate){
        LocalDate effectiveStartDate = (startAssignmentDate != null)
                                            ? startAssignmentDate
                                            : LocalDate.now(zoneId); 
        Optional<Shift> shiftOpt = shiftRepository.findNextUnplannedShift(
                    ShiftStatus.UNPLANNED,
                    shiftRequestExternalId,
                    effectiveStartDate);
        return shiftOpt.map(shift -> ShiftDto.fromEntity(shift, zoneId));
    }

    @Transactional(readOnly = true)
    public List<ShiftDto> getUpComingShiftsByShiftRequest(
                            ZoneId zoneId,
                            UUID shiftRequestExternalId,
                            LocalDate queryDate,
                            int limit
                        ){
        Pageable pageable = PageRequest.of(0, limit);
        LocalDate effectiveDate = (queryDate != null) ? queryDate : LocalDate.now();
        List<ShiftProjection> shifts = shiftRepository
            .findUpcomingShiftsByShiftRequest(
                null, shiftRequestExternalId, effectiveDate, pageable);
        return shifts.stream()
                .map(shift -> ShiftDto.fromProjection(shift, zoneId))
                .toList();
    }

    @Transactional
    public Shift assignAndStartShift(UUID userExternalId, OffsetDateTime punchTs){
        if (userExternalId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado");
        }
        if (punchTs == null) {
            throw new IllegalArgumentException("El timestamp de marcación es obligatorio");
        }
        // Verificar si ya tiene un turno activo
        Optional<Shift> activeShiftOpt = shiftRepository.findActiveShiftToAssign(userExternalId);
        if(activeShiftOpt.isPresent()) return activeShiftOpt.get();
        // Definir ventana de marcación.
        OffsetDateTime startWindow = punchTs.minusHours(2);
        OffsetDateTime endWindow = punchTs.plusHours(2);
        Optional<Shift> shiftOpt = shiftRepository.findFirstShiftToAssign(
                                            userExternalId, startWindow, endWindow);
        if (shiftOpt.isPresent()){
            Shift shift = shiftOpt.get();
            shift.setStatus(ShiftStatus.IN_PROGRESS);
            shift.setStartExecutionTs(punchTs);
            return shiftRepository.save(shift);
        }
        return null;
    }

    @Transactional
    public Shift completeShift (UUID userExternalId, OffsetDateTime punchTs){
        if (userExternalId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado");
        }
        if (punchTs == null) {
            throw new IllegalArgumentException("El timestamp de marcación es obligatorio");
        }
        Optional<Shift> activeShiftOpt = shiftRepository.findActiveShiftToAssign(userExternalId);
        if(activeShiftOpt.isPresent()){
            Shift shift = activeShiftOpt.get();
            shift.setStatus(ShiftStatus.COMPLETED);
            shift.setEndExecutionTs(punchTs);
            return shiftRepository.save(shift);
        }
        return null;
    }
}
