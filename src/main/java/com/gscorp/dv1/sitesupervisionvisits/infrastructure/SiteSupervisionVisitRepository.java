package com.gscorp.dv1.sitesupervisionvisits.infrastructure;

import java.util.List;
import java.util.Optional;

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

    @EntityGraph(attributePaths = {"employee", "site"})
    @Query("select v from SiteSupervisionVisit v where v.id = :id")
    Optional<SiteSupervisionVisit> findByIdWithEmployeeAndSite(Long id);

}
