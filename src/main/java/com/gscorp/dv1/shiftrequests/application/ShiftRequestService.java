package com.gscorp.dv1.shiftrequests.application;

import java.util.List;
import java.util.Optional;

import com.gscorp.dv1.shiftrequests.infrastructure.ShiftRequest;

public interface ShiftRequestService {
    List<ShiftRequest> findAll();
    Optional<ShiftRequest> findById(Long id);
    ShiftRequest create(ShiftRequest shiftRequest);
    ShiftRequest update(ShiftRequest shiftRequest);
}
