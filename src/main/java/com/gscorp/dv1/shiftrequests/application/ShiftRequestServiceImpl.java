package com.gscorp.dv1.shiftrequests.application;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.gscorp.dv1.shiftrequests.infrastructure.ShiftRequest;
import com.gscorp.dv1.shiftrequests.infrastructure.ShiftRequestRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ShiftRequestServiceImpl implements ShiftRequestService {

    private final ShiftRequestRepository shiftRequestRepository;
    
    @Override
    public List<ShiftRequest> findAll() {
        return shiftRequestRepository.findAll();
    }

    @Override
    public Optional<ShiftRequest> findById(Long id) {
        return shiftRequestRepository.findById(id);
    }

    @Override
    public ShiftRequest create(ShiftRequest shiftRequest) {
        return shiftRequestRepository.save(shiftRequest);
    }

    @Override
    public ShiftRequest update(ShiftRequest shiftRequest) {
        return shiftRequestRepository.save(shiftRequest);
    }
}
