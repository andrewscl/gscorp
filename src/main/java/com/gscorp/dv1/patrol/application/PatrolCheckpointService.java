package com.gscorp.dv1.patrol.application;

import java.util.List;

import com.gscorp.dv1.patrol.web.dto.PatrolCheckpointDto;

public interface PatrolCheckpointService {
    
    List<PatrolCheckpointDto> findAllBySite(Long siteId);

    List<PatrolCheckpointDto> findAll();
}
