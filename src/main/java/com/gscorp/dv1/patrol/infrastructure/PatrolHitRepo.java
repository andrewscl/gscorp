package com.gscorp.dv1.patrol.infrastructure;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PatrolHitRepo extends JpaRepository<PatrolHit, Long>{

    List<PatrolHit> findByRunIdOrderByTsAsc(Long runId);
    
}
