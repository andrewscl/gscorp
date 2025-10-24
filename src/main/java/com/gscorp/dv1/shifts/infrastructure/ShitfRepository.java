package com.gscorp.dv1.shifts.infrastructure;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShitfRepository extends JpaRepository<Shift, Long>{

    List<Shift> findBySiteIdAndStartTsBetween(Long siteId, OffsetDateTime from, OffsetDateTime to);
    
}
