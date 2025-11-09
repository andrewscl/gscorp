package com.gscorp.dv1.sites.infrastructure;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SiteRepository extends JpaRepository<Site, Long>{

    List<Site> findByProjectId(Long projectId);
    long countByProjectId(Long projectId);

    @Query("SELECT s FROM Site s JOIN FETCH s.project")
    List<Site> findAllWithProjects();

    @EntityGraph(attributePaths = "project")
    Optional<Site> findById(Long id);

    List<Site> findByClientIdIn (List<Long> clientIds);

    
}
