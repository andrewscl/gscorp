package com.gscorp.dv1.positions.application;

import java.util.List;

import org.springframework.stereotype.Service;

import com.gscorp.dv1.positions.infrastructure.Position;
import com.gscorp.dv1.positions.infrastructure.PositionProjection;
import com.gscorp.dv1.positions.infrastructure.PositionRepository;
import com.gscorp.dv1.positions.web.dto.PositionDto;

import jakarta.transaction.Transactional;
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

    @Override
    @Transactional
    public List<PositionDto> findAllProjection() {

        List<PositionProjection> projections = positionRepository.findAllProjection();

        List<PositionDto> result = projections.stream()
            .map(PositionDto::fromProjection)
            .toList();

        return result;
    }
}
