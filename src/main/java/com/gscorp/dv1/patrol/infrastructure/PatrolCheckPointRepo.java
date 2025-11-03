package com.gscorp.dv1.patrol.infrastructure;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PatrolCheckPointRepo extends JpaRepository<PatrolCheckPoint, Long> {

    List<PatrolCheckPoint> findBySiteId(Long siteId);
    
}
