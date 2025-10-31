package com.gscorp.dv1.sitesupervisionvisits.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SiteSupervisionVisitRepository
        extends JpaRepository<SiteSupervisionVisit, Long> {
    
}
