package com.gscorp.dv1.requests.application;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.gscorp.dv1.requests.infrastructure.ShiftRequest;
import com.gscorp.dv1.requests.infrastructure.ShiftRequestRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ShiftRequestServiceImpl
                    implements ShiftRequestService {

    private final ShiftRequestRepository shiftRequestRepository;

    @Override
    @Transactional
    public ShiftRequest saveShiftRequest(ShiftRequest shiftRequest) {
        return shiftRequestRepository.save(shiftRequest);
    }

    @Override
    @Transactional
    public Optional<ShiftRequest> findById(Long id) {
        return shiftRequestRepository.findById(id);
    }
    
}
