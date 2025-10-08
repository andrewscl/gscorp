package com.gscorp.dv1.guards.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gscorp.dv1.guards.infrastructure.GuardRepo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GuardService {

    private final GuardRepo repo;

    @Transactional(readOnly = true)
    public Long activeByClient(Long clientId) {
        return repo.countBySite_Client_IdAndActiveTrue(clientId);
    }
    
}
