package com.gscorp.dv1.requests.application;

import java.util.List;
import java.util.Optional;

import com.gscorp.dv1.requests.infrastructure.ShiftRequest;

public interface ShiftRequestService {
    List<ShiftRequest> findAll();
    Optional<ShiftRequest> findById(Long id);
    ShiftRequest create(ShiftRequest shiftRequest);
    ShiftRequest update(ShiftRequest shiftRequest);
}
