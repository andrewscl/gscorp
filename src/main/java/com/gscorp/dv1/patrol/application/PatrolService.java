package com.gscorp.dv1.patrol.application;

import java.util.List;

import com.gscorp.dv1.patrol.web.dto.PatrolDto;

public interface PatrolService {

    List<PatrolDto> getPatrolsByUserId(Long userId);

}
