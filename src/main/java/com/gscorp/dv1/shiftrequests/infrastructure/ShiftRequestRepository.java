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
    String findLastCodeBySiteIdAndPrefix(Long siteId, String prefix);

    // Cargar ShiftRequest + Site + Schedules (si necesitas schedules en la tabla/listado)
    @Query("select distinct s " +
           "from ShiftRequest s " +
           "join fetch s.site st " +
           "left join fetch s.schedules sch " +
           "where st.client.id in :clientIds " +
           "order by s.code")
    List<ShiftRequest> findBySiteClientIdInFetchSiteAndSchedules(
                                    @Param("clientIds") Collection<Long> clientIds);

    // Cargar todos (admin)
    @Query("select distinct s " +
           "from ShiftRequest s " +
           "join fetch s.site st " +
           "left join fetch s.schedules sch " +
           "order by s.code")
    List<ShiftRequest> findAllWithSiteAndSchedules();

    // Cargar uno por id con sus relaciones
    @Query("select distinct s " +
           "from ShiftRequest s " +
           "join fetch s.site st " +
           "left join fetch s.schedules sch " +
           "where s.id = :id")
    Optional<ShiftRequest> findByIdWithSiteAndSchedules(@Param("id") Long id);

}
