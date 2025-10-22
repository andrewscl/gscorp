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
    
    @Override
    public java.util.Set<Position> findAll() {
        return new java.util.HashSet<>(positionRepository.findAll());
    }

    @Override
    public Position savePosition(Position position) {
        return positionRepository.save(position);
    }
}
