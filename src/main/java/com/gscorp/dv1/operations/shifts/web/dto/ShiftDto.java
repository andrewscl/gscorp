package com.gscorp.dv1.operations.shifts.web.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

import com.gscorp.dv1.enums.DayOfWeek;
import com.gscorp.dv1.enums.ShiftStatus;
import com.gscorp.dv1.operations.shifts.infrastructure.Shift;
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
        String schedule = formatSchedule(sp.getStartTs(), sp.getEndTs(), zoneId);
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
                    formatFullName(sp.getEmployeeName(), sp.getEmployeeFatherSurname())
        );
    }

    public static ShiftDto fromEntity (Shift shift, ZoneId zoneId){
        if (shift == null) return null;
        String schedule = formatSchedule(shift.getStartTs(), shift.getEndTs(), zoneId);
        DayOfWeek day = shift.getShiftDate() != null
                        ? DayOfWeek.fromJavaTime(shift.getShiftDate().getDayOfWeek())
                        : null;
        var employee = Optional.ofNullable(shift.getAssignment())
                            .map(s -> shift.getAssignment().getEmployee())
                            .orElse(null);
        String employeeName = employee != null ? employee.getName() : null;
        String employeeSurname = employee != null ? employee.getFatherSurname() : null;
        String siteName = shift.getSite() != null ? shift.getSite().getName() : null;
        String requestCode = shift.getShiftRequest() != null ? shift.getShiftRequest().getCode() : null;
        return new ShiftDto(
                    shift.getId(),
                    shift.getExternalId(),
                    shift.getShiftDate(),
                    shift.getStartTs(),
                    shift.getEndTs(),
                    schedule,
                    shift.getStatus(),
                    day,
                    siteName,
                    requestCode,
                    formatFullName(employeeName, employeeSurname)
        );
    }

    private static String formatFullName(String name, String surname) {
        if (name == null && surname == null) return null;
        return ((name != null ? name : "") + " " + (surname != null ? surname : "")).trim();
    }

    private static String formatSchedule(OffsetDateTime startTs, OffsetDateTime endTs, ZoneId zoneId) {
        if (startTs == null || endTs == null) return "-";
        ZoneId zone = zoneId != null ? zoneId : ZoneId.systemDefault();
        String startFormatted = startTs.atZoneSameInstant(zone).format(TIME_FORMATTER);
        String endFormatted = endTs.atZoneSameInstant(zone).format(TIME_FORMATTER);
        return startFormatted + " - " + endFormatted;
    }

    public String getStatusDisplayName() {
        return (status != null) ? status.getDisplayName() : null; 
    }

}
