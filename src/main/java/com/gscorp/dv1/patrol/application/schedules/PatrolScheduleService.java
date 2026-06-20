package com.gscorp.dv1.patrol.application.schedules;

import java.util.List;
import java.util.UUID;

import com.gscorp.dv1.patrol.web.schedules.dto.PatrolScheduleDto;

public interface PatrolScheduleService {
    
    List<PatrolScheduleDto> getNext24hPatrolSchedulesBySiteExternalId (
                UUID siteExternalId);

}
