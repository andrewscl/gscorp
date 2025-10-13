package com.gscorp.dv1.guards.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GuardRepo extends JpaRepository<Guard, Long>{

  Long countBySite_Project_Client_IdAndActiveTrue(Long clientId);

  Guard findByUserId(Long userId);
    
}
