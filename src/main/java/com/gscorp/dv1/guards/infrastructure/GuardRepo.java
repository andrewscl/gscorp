package com.gscorp.dv1.guards.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface GuardRepo extends JpaRepository<Guard, Long>{

  /** Dotación activa del cliente (ajusta si tu entidad no tiene campo active). */
  @Query("""
    select count(distinct g.id)
    from Guard g
    where g.site.client.id = :clientId
      and (g.active = true or g.active is null)
  """)
  Long activeByClient(@Param("clientId") Long clientId);

  Guard findByUserId(Long userId);
    
}
