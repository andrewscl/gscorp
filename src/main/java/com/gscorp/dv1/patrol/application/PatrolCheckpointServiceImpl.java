package com.gscorp.dv1.patrol.application;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gscorp.dv1.patrol.infrastructure.PatrolCheckPointRepo;
import com.gscorp.dv1.patrol.web.dto.PatrolCheckpointDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PatrolCheckpointServiceImpl implements PatrolCheckpointService{
    
    private final PatrolCheckPointRepo patrolCheckPointRepo;

    @Override
    @Transactional(readOnly = true)
    public List<PatrolCheckpointDto> findAllBySite(Long siteId) {
        return patrolCheckPointRepo.findBySiteId(siteId)
            .stream()
            .map(cp -> new PatrolCheckpointDto(
                cp.getId(),
                cp.getSite().getId(),
                cp.getSite().getName(),
                cp.getRoute().getId(),
                cp.getRoute().getName(),
                cp.getName(),
                cp.getLat(),
                cp.getLon(),
                cp.getOrderN(),
                cp.getToleranceM()
            ))
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatrolCheckpointDto> findAll() {
        return patrolCheckPointRepo.findAll()
            .stream()
            .map(cp -> new PatrolCheckpointDto(
                cp.getId(),
                cp.getSite().getId(),
                cp.getSite().getName(),
                cp.getRoute().getId(),
                cp.getRoute().getName(),
                cp.getName(),
                cp.getLat(),
                cp.getLon(),
                cp.getOrderN(),
                cp.getToleranceM()
            ))
            .toList();
    }

}
