package com.gscorp.dv1.sitesupervisionvisits.infrastructure;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SiteSupervisionVisitRepository
        extends JpaRepository<SiteSupervisionVisit, Long> {

    @EntityGraph(attributePaths = {"employee", "site"})
    @Query("select v from SiteSupervisionVisit v")
    List<SiteSupervisionVisit> findAllWithEmployeeAndSite();
    
}
