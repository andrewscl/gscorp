package com.gscorp.dv1.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gscorp.dv1.entities.Licitation;

@Repository
public interface LicitationRepository extends JpaRepository <Licitation, String>{
    
}
