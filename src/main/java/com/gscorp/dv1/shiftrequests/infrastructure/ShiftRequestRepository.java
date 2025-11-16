package com.gscorp.dv1.shiftrequests.infrastructure;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ShiftRequestRepository extends JpaRepository<ShiftRequest, Long>{

    @Query("SELECT sr.code FROM ShiftRequest sr WHERE sr.site.id = :siteId AND sr.code LIKE CONCAT(:prefix, '%') ORDER BY sr.code DESC")
    String findLastCodeBySiteIdAndPrefix(@Param("siteId") Long siteId, @Param("prefix") String prefix);

    // Cargar ShiftRequest + Site + Schedules (filtrar por client ids recorriendo project -> client)
    @Query("select distinct sr " +
           "from ShiftRequest sr " +
           "join fetch sr.site st " +
           "left join fetch sr.schedules sch " +
           "where st.project.client.id in :clientIds " +
           "order by sr.code")
    List<ShiftRequest> findBySiteClientIdInFetchSiteAndSchedules(@Param("clientIds") Collection<Long> clientIds);

    // Cargar todos (admin)
    @Query("select distinct sr " +
           "from ShiftRequest sr " +
           "join fetch sr.site st " +
           "left join fetch sr.schedules sch " +
           "order by sr.code")
    List<ShiftRequest> findAllWithSiteAndSchedules();

    // Cargar uno por id con sus relaciones
    @Query("select distinct sr " +
           "from ShiftRequest sr " +
           "join fetch sr.site st " +
           "left join fetch sr.schedules sch " +
           "where sr.id = :id")
    Optional<ShiftRequest> findByIdWithSiteAndSchedules(@Param("id") Long id);

}
