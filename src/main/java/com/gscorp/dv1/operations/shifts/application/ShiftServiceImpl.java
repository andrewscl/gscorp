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
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gscorp.dv1.enums.DayOfWeek;
import com.gscorp.dv1.enums.ShiftRequestStatus;
import com.gscorp.dv1.enums.ShiftStatus;
import com.gscorp.dv1.operations.shiftrequests.infrastructure.ShiftRequest;
import com.gscorp.dv1.operations.shiftrequests.infrastructure.ShiftRequestRepository;
import com.gscorp.dv1.operations.shiftrequests.infrastructure.ShiftRequestScheduleRepository;
import com.gscorp.dv1.operations.shiftrequests.infrastructure.projections.ShiftRequestScheduleProjection;
import com.gscorp.dv1.operations.shifts.infrastructure.Shift;
import com.gscorp.dv1.operations.shifts.infrastructure.ShitfRepository;
import com.gscorp.dv1.operations.shifts.infrastructure.projections.ShiftProjection;
import com.gscorp.dv1.operations.shifts.web.dto.ShiftDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ShiftServiceImpl implements ShiftService {

    private final ShitfRepository shiftRepository;
    private final ShiftRequestScheduleRepository shiftRequestScheduleRepository;
    private final ShiftRequestRepository shiftRequestRepository;

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
                            .shiftStatus(ShiftStatus.PLANNED)
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
        }
    }


    @Transactional(readOnly = true)
    public Page<ShiftDto> getLastFiveShiftsByShiftRequest(
                                    UUID shiftRequestExternalId) {
        
        Pageable pageable =
                        PageRequest.of(0, 5 );

        Page<ShiftProjection> projections =
                    shiftRepository.findLastFiveByShiftRequestExternalId(
                                            shiftRequestExternalId, pageable);

        return projections.map(ShiftDto::fromProjection);
    }

}
