package com.gscorp.dv1.core.positions.application;

import java.util.List;
import java.util.Set;

import com.gscorp.dv1.core.positions.infrastructure.Position;
import com.gscorp.dv1.core.positions.web.dto.PositionDto;

public interface PositionService {

    Position findById(Long id);
    Set<Position> findAll();
    Position savePosition(Position position);

    List<PositionDto> findAllProjection();


}
