package com.gscorp.dv1.sites.infrastructure;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.gscorp.dv1.sites.web.dto.SiteSelectDto;

@Repository
public interface SiteRepository extends JpaRepository<Site, Long>{

    List<Site> findByProjectId(Long projectId);
    long countByProjectId(Long projectId);

    @Query("SELECT s FROM Site s JOIN FETCH s.project")
    List<Site> findAllWithProjects();

    @EntityGraph(attributePaths = "project")
    Optional<Site> findById(Long id);

    List<Site> findByProject_Client_IdIn (List<Long> clientIds);

    @Query("select new com.gscorp.dv1.sites.web.dto.SiteSelectDto(s.id, s.name) " +
           "from Site s where s.project.client.id in :clientIds order by s.name")
    List<SiteSelectDto> findSelectDtoByClientIds(@Param("clientIds") Collection<Long> clientIds);

    // Devuelve solo el client id asociado al site (puede ser vacío si no existe la relación)
    @Query("select s.project.client.id from Site s where s.id = :id")
    Optional<Long> findClientIdBySiteId(@Param("id") Long id);

}
