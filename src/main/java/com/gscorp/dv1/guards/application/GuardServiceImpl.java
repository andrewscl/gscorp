package com.gscorp.dv1.guards.application;

import org.springframework.stereotype.Service;

import com.gscorp.dv1.guards.infrastructure.GuardRepo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GuardServiceImpl implements GuardService{

    private final GuardRepo repo;

    public Long activeByClient(Long clientId) {
        return repo.countBySite_Project_Client_IdAndActiveTrue(clientId);
    }

}
