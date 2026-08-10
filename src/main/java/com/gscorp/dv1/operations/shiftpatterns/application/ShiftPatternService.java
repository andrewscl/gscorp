package com.gscorp.dv1.operations.shiftpatterns.application;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.gscorp.dv1.operations.shiftpatterns.infrastructure.ShiftPattern;
import com.gscorp.dv1.operations.shiftpatterns.web.dto.ShiftPatternDto;
import com.gscorp.dv1.operations.shiftpatterns.web.dto.UpdateShiftPatternRequest;

public interface ShiftPatternService {

    ShiftPattern findById(Long id);

    ShiftPatternDto findDtoByExternalId(UUID externalId);

    ShiftPattern findByExternalId(UUID externalId);

    Set<ShiftPattern> findAll();

    ShiftPattern saveShiftPattern(ShiftPattern shiftPattern);

    List<ShiftPatternDto> getShiftPatternsList();

    ShiftPatternDto updateShiftPattern(
            UUID shiftPatternExternalId,
            UpdateShiftPatternRequest updateShiftPatternRequest);


}
