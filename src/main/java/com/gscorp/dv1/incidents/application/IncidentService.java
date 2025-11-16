package com.gscorp.dv1.incidents.application;

import java.time.LocalDate;
import java.util.List;

import com.gscorp.dv1.incidents.infrastructure.IncidentRepository;
import com.gscorp.dv1.incidents.web.dto.CreateIncidentRequest;
import com.gscorp.dv1.incidents.web.dto.IncidentDto;

public interface IncidentService {
    
    List<IncidentRepository.DayCount> byDayForClient(Long clientId, LocalDate from, LocalDate to);
    List<IncidentDto> findAll();
    IncidentDto findById(Long id);
    IncidentDto createIncident(CreateIncidentRequest request);
    List<IncidentDto> findIncidentsForUser(Long userId);

    long countOpenByClientIds(List<Long> clientIds);

}
