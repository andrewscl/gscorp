package com.gscorp.dv1.shiftrequests.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ShiftRequestRepository extends JpaRepository<ShiftRequest, Long>{

    @Query("SELECT sr.code FROM ShiftRequest sr WHERE sr.site.id = :siteId AND sr.code LIKE CONCAT(:prefix, '%') ORDER BY sr.code DESC")
    String findLastCodeBySiteIdAndPrefix(Long siteId, String prefix);
    
}
