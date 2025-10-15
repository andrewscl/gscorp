package com.gscorp.dv1.licitations.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LicitationRepository extends JpaRepository <Licitation, String>{
    
}
