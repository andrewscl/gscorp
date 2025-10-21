package com.gscorp.dv1.shiftpatterns.application;

import com.gscorp.dv1.shiftpatterns.infrastructure.ShiftPattern;

public interface ShiftPatternService {

    ShiftPattern findById(Long id);
}
