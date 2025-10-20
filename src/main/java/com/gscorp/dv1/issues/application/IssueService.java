package com.gscorp.dv1.issues.application;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gscorp.dv1.issues.infrastructure.IssueRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class IssueService {
    
  private final IssueRepository repo;

  @Transactional(readOnly = true)
  public List<IssueRepository.DayCount> byDayForClient(Long clientId, LocalDate from, LocalDate to) {
    return repo.byDayForClient(clientId, from, to);
  }

}
