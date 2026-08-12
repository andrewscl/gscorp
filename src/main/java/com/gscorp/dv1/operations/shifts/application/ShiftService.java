package com.gscorp.dv1.operations.shifts.application;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;

import com.gscorp.dv1.config.security.SecurityUser;
import com.gscorp.dv1.enums.ShiftStatus;
import com.gscorp.dv1.operations.shiftrequests.infrastructure.ShiftRequest;
import com.gscorp.dv1.operations.shifts.infrastructure.Shift;
import com.gscorp.dv1.operations.shifts.web.dto.ShiftDto;
import com.gscorp.dv1.operations.shifts.web.dto.ShiftsCountLast24HoursDto;

public interface ShiftService {

    List<Shift> getShifts(
                    Long siteId,
                    OffsetDateTime from,
                    OffsetDateTime to);

    Shift createShift(Shift shift);

    void generateShiftsForNext30days(
                        ShiftRequest shiftRequest,
                        String username,
                        ZoneId zone);

    void processApprovedShiftRequests();

    Page<ShiftDto> getLastShiftsByShiftRequest(
                            UUID shiftRequestExternalId,
                            int shiftsToShow);

    List<ShiftsCountLast24HoursDto> getShiftsCountLast24Hours(
                                                UUID userExternalId);

    List<ShiftDto> getUpcomingByShiftAssignmentExternalId(
                                        UUID userExternalId,
                                        UUID shiftAssignmentExternalId,
                                        Integer shiftsToShow);

    Page<ShiftDto> getShiftList(
                        SecurityUser securityUser,
                        LocalDate startDate,
                        LocalDate endDate,
                        Long projectId,
                        Long siteId,
                        ShiftStatus status,
                        int page, int size);

    
}
