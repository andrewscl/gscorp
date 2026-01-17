package com.gscorp.dv1.patrol.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PatrolCheckPointRepo extends JpaRepository<PatrolCheckpoint, Long> {


    
}
