package com.gscorp.dv1.incidents.application;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gscorp.dv1.incidents.infrastructure.IncidentRepo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class IncidentService {
    
  private final IncidentRepo repo;

  @Transactional(readOnly = true)
  public List<IncidentRepo.DayCount> byDayForClient(Long clientId, LocalDate from, LocalDate to) {
    return repo.byDayForClient(clientId, from, to);
  }

}
