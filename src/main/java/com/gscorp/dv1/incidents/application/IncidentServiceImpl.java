package com.gscorp.dv1.incidents.application;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gscorp.dv1.incidents.infrastructure.IncidentRepository;
import com.gscorp.dv1.incidents.web.dto.IncidentDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class IncidentServiceImpl implements IncidentService {
    
  private final IncidentRepository repo;

  @Transactional(readOnly = true)
  public List<IncidentRepository.DayCount> byDayForClient(Long clientId, LocalDate from, LocalDate to) {
    return repo.byDayForClient(clientId, from, to);
  }

  @Override
  @Transactional(readOnly = true)
  public List<IncidentDto> findAll() {
    return repo.findAll().stream()
        .map(IncidentDto::fromEntity)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public IncidentDto findById(Long id) {
    return repo.findById(id).map(IncidentDto::fromEntity).orElse(null);
  }

}
