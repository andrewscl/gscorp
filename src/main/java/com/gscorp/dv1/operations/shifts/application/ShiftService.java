package com.gscorp.dv1.operations.shifts.application;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

import com.gscorp.dv1.operations.shiftrequests.infrastructure.ShiftRequest;
import com.gscorp.dv1.operations.shifts.infrastructure.Shift;

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

}
