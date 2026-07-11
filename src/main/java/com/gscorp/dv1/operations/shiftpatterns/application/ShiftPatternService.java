package com.gscorp.dv1.operations.shiftpatterns.application;

import java.util.Set;

import com.gscorp.dv1.operations.shiftpatterns.infrastructure.ShiftPattern;

public interface ShiftPatternService {

    ShiftPattern findById(Long id);

    Set<ShiftPattern> findAll();

    ShiftPattern saveShiftPattern(ShiftPattern shiftPattern);
}
