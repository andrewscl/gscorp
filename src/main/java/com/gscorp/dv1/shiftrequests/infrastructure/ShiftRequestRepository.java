package com.gscorp.dv1.shiftrequests.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShiftRequestRepository extends JpaRepository<ShiftRequest, Long>{

    String findLastCodeBySiteIdAndPrefix(Long siteId, String prefix);
    
}
