package com.gscorp.dv1.sites.infrastructure;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SiteRepository extends JpaRepository<Site, Long>{

    List<Site> findByClientId(Long clientId);
    long countByClientId(Long clientId);

    @Query("SELECT s FROM Site s JOIN FETCH s.client")
    List<Site> findAllWithClients();
    
}
