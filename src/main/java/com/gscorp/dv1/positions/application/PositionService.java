package com.gscorp.dv1.positions.application;

import java.util.List;
import java.util.Set;

import com.gscorp.dv1.positions.infrastructure.Position;
import com.gscorp.dv1.positions.web.dto.PositionDto;

public interface PositionService {

    Position findById(Long id);
    Set<Position> findAll();
    Position savePosition(Position position);

    List<PositionDto> findAllProjection();


}
