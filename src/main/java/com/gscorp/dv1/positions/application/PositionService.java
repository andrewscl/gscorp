package com.gscorp.dv1.positions.application;

import com.gscorp.dv1.positions.infrastructure.Position;

public interface PositionService {

    Position findById(Long id);
    
}
