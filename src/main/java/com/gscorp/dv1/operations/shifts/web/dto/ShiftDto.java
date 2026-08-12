package com.gscorp.dv1.operations.shifts.web.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import com.gscorp.dv1.enums.DayOfWeek;
import com.gscorp.dv1.enums.ShiftStatus;
import com.gscorp.dv1.operations.shifts.infrastructure.projections.ShiftProjection;

public record ShiftDto (
    Long id,
    UUID externalId,
    LocalDate shiftDate,
    OffsetDateTime startTs,
    OffsetDateTime endTs,
    String schedule,
    ShiftStatus status,
    DayOfWeek day,
    String siteName,
    String shiftRequestCode,
    String plannedEmployeeFullName
){
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public static ShiftDto fromProjection(ShiftProjection sp, ZoneId zoneId){
        if ( sp == null) return null;
        String name = sp.getEmployeeName();
        String surname = sp.getEmployeeFatherSurname();
        String employeeFullName = null;
        if (name != null || surname != null) {
            employeeFullName = ((name != null ? name : "") + " " + (surname != null ? surname : "" )).trim();
        }
        String schedule = "-";
        if(sp.getStartTs() != null && sp.getEndTs() != null){
            String startFormatted = sp.getStartTs().atZoneSameInstant(zoneId).format(TIME_FORMATTER);
            String endFormatted = sp.getEndTs().atZoneSameInstant(zoneId).format(TIME_FORMATTER);
            schedule = startFormatted + " - " + endFormatted;
        }
        DayOfWeek day = sp.getShiftDate() != null
                        ? DayOfWeek.fromJavaTime(sp.getShiftDate().getDayOfWeek())
                        : null;
        return new ShiftDto(
            sp.getId(),
            sp.getExternalId(),
            sp.getShiftDate(),
            sp.getStartTs(),
            sp.getEndTs(),
            schedule,
            sp.getStatus(),
            day,
            sp.getSiteName(),
            sp.getShiftRequestCode(),
            employeeFullName
        );
    }
}
