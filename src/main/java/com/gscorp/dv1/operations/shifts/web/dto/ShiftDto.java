package com.gscorp.dv1.operations.shifts.web.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;
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
    ShiftStatus status,
    DayOfWeek day,
    String siteName,
    String shiftRequestCode,
    String employeeFullName
){
    public static ShiftDto fromProjection(ShiftProjection sp){
        if ( sp == null) return null;
        String name = sp.getEmployeeName();
        String surname = sp.getEmployeeFatherSurname();
        String employeeFullName = null;
        if (name != null || surname != null) {
            employeeFullName = 
                ((name != null ? name : "") + " " + (surname != null ? surname : "" ))
                .trim();
        }
        return new ShiftDto(
            sp.getId(),
            sp.getExternalId(),
            sp.getShiftDate(),
            sp.getStartTs(),
            sp.getEndTs(),
            sp.getStatus(),
            sp.getShiftDate() != null
                ? DayOfWeek.fromJavaTime(sp.getShiftDate().getDayOfWeek())
                : null,
            sp.getSiteName(),
            sp.getShiftRequestCode(),
            employeeFullName
        );
    }
}
