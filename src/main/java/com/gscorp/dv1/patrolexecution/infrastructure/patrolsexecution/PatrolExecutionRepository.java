package com.gscorp.dv1.patrolexecution.infrastructure.patrolsexecution;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PatrolExecutionRepository
                        extends JpaRepository <PatrolExecution, Long>{
    
}
