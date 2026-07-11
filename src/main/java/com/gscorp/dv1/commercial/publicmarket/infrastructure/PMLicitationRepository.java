package com.gscorp.dv1.commercial.publicmarket.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PMLicitationRepository extends JpaRepository <PublicMarketLicitation, String>{
    
}
