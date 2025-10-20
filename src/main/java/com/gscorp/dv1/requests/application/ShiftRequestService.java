package com.gscorp.dv1.requests.application;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.gscorp.dv1.requests.infrastructure.ShiftRequest;

@Service
public interface ShiftRequestService {

    ShiftRequest saveShiftRequest (ShiftRequest shiftRequest);

    Optional<ShiftRequest> findById (Long id);
}
