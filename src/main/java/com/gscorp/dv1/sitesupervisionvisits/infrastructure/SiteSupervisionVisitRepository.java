package com.gscorp.dv1.sitesupervisionvisits.infrastructure;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Query("select v from SiteSupervisionVisit v "
         + "where v.site.client.id = :clientId "
         + "and v.visitDate between :fromDate and :toDate")
    List<SiteSupervisionVisit> findByClientIdAndDateBetween(
        @Param("clientId") Long clientId,
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate);

}
