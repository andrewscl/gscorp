package com.gscorp.dv1.positions.application;

import java.util.Set;

import com.gscorp.dv1.positions.infrastructure.Position;

public interface PositionService {

    Position findById(Long id);
    Set<Position> findAll();
    Position savePosition(Position position);
    
}
