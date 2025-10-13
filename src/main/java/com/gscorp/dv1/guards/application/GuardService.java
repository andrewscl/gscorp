package com.gscorp.dv1.guards.application;

import org.springframework.stereotype.Service;

@Service
public interface GuardService {

Long activeByClient(Long clientId);
    
}
