package com.gscorp.dv1.requests.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShiftRequestRepository extends JpaRepository<ShiftRequest, Long>{
    
}
