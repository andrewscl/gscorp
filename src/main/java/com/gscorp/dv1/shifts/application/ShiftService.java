package com.gscorp.dv1.shifts.application;

import java.time.OffsetDateTime;
import java.util.List;

import com.gscorp.dv1.shifts.infrastructure.Shift;

public interface ShiftService {

    List<Shift> getShifts(Long siteId, OffsetDateTime from, OffsetDateTime to);

}
