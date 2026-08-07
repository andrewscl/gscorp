package com.gscorp.dv1.operations.shiftpatterns.application;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.gscorp.dv1.operations.shiftpatterns.infrastructure.ShiftPattern;
import com.gscorp.dv1.operations.shiftpatterns.web.dto.ShiftPatternDto;

public interface ShiftPatternService {

    ShiftPattern findById(Long id);

    Optional<ShiftPatternDto> findByExternalId(UUID externalId);

    Set<ShiftPattern> findAll();

    ShiftPattern saveShiftPattern(ShiftPattern shiftPattern);

    List<ShiftPatternDto> getShiftPatternsList();
    
}
