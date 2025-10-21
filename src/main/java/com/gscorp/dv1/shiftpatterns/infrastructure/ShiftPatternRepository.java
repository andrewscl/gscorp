package com.gscorp.dv1.shiftpatterns.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShiftPatternRepository extends JpaRepository<ShiftPattern, Long> {
    
}
