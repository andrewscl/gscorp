package com.gscorp.dv1.positions.application;

import org.springframework.stereotype.Service;

import com.gscorp.dv1.positions.infrastructure.Position;
import com.gscorp.dv1.positions.infrastructure.PositionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PositionServiceImpl implements PositionService {

    private final PositionRepository positionRepository;

    @Override
    public Position findById(Long id) {
        return positionRepository.findById(id).orElse(null);
    }
    
}
