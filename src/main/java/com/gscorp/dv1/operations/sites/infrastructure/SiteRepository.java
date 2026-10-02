package com.gscorp.dv1.operations.sites.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SiteRepository extends JpaRepository<Site, Long>{

    List<Site> findByProjectId(Long projectId);
    long countByProjectId(Long projectId);

    @Query("SELECT s FROM Site s JOIN FETCH s.project")
    List<Site> findAllWithProjects();

    @EntityGraph(attributePaths = "project")
    Optional<Site> findById(Long id);


    @Query("""
        SELECT s
        FROM Site s
        JOIN s.project p
        WHERE (:ignoreProjectFilter = true OR p.id IN :projectIds)
        AND (:externalId = s.externalId)
        """)
    Optional<Site> findByExternalId(
        @Param("ignoreProjectFilter") boolean ignoreProjectFilter,
        @Param("projectIds") List<Long> projectIds,
        @Param("externalId") UUID externalId
    );


    @Query("SELECT s FROM Site s JOIN FETCH s.project WHERE s.id = :id")
    Optional<Site> findByIdWithProject(Long id);

    List<Site> findByProject_Client_IdIn(List<Long> clientIds);


    // Devuelve solo el client id asociado al site (puede ser vacío si no existe la relación)
    @Query("select s.project.client.id from Site s where s.id = :id")
    Optional<Long> findClientIdBySiteId(@Param("id") Long id);

    @Query("""
        SELECT
            s.id          AS id,
            s.externalId  AS externalId,
            s.name        AS name,
            s.address     AS address,
            s.lat         AS lat,
            s.lon         AS lon,
            s.timeZone    AS timeZone
        FROM Site s
        JOIN s.project p
        WHERE (:ignoreProjectFilter = true OR p.id IN :projectIds)
        AND (p.externalId IN :projectExternalId)
        ORDER BY s.name
        """)
    List<SiteProjection> findProjectionsByProjectExternalId(
            @Param("ignoreProjectFilter") boolean ignoreProjectFilter,
            @Param("projectIds") List<Long> projectIds,
            @Param("projectExternalId") UUID projectExternalId);


    @Query("""
        SELECT DISTINCT
          s.id   AS id,
          s.externalId AS externalId,
          s.name AS name,
          s.lat  AS lat,
          s.lon  AS lon
        FROM Site s
        LEFT JOIN s.project p
        WHERE p.client.id IN :clientIds
        ORDER BY s.name
        """)
    List<SiteProjection> findByClientIds(@Param("clientIds") List<Long> clientIds);


    @Query("""
        SELECT DISTINCT
          s.id   AS id,
          s.externalId AS externalId,
          s.name AS name,
          s.lat  AS lat,
          s.lon  AS lon
        FROM Site s
        JOIN s.project p
        WHERE p.id IN :projectIds
        ORDER BY s.name
        """)
    List<SiteProjection> findByProjectIds(@Param("projectIds") List<Long> projectIds);



    @Query("""
        SELECT DISTINCT
          s.id          AS id,
          s.externalId  AS externalId,
          s.name        AS name,
          s.address     AS address,
          s.lat         AS lat,
          s.lon         AS lon,
          s.timeZone    AS timeZone
        FROM Site s
        JOIN s.project p
        WHERE p.client.id IN :clientIds
        ORDER BY s.name
        """)
    List<SiteProjection> findSiteProjectionsByClientIds(
        @Param("clientIds") List<Long> clientIds
    );

    Optional<SiteProjection> findProjectionById(Long id);

    @Query("""
        SELECT DISTINCT
          s.id          AS id,
          s.externalId  AS externalId,
          s.name        AS name,
          s.address     AS address,
          s.lat         AS lat,
          s.lon         AS lon,
          s.timeZone    AS timeZone,
          p.id          AS projectId,
          p.name        AS projectName,
          s.status      AS status,
          s.active      AS active
        FROM Site s
        JOIN s.project p
        WHERE (:ignoreProjectFilter = true OR p.id IN :projectIds)
        ORDER BY s.name
        """)
    Optional<SiteProjection> findByUserScope(
        @Param("ignoreProjectFilter") boolean ignoreProjectFilter,
        @Param("projectIds") List<Long> projectIds
    );

    @Query("""
        SELECT DISTINCT
          s.id          AS id,
          s.externalId  AS externalId,
          s.name        AS name,
          s.address     AS address,
          s.lat         AS lat,
          s.lon         AS lon,
          s.timeZone    AS timeZone,
          p.id          AS projectId,
          p.name        AS projectName,
          s.status      AS status,
          s.active      AS active
        FROM Site s
        JOIN s.project p
        WHERE (:ignoreProjectFilter = true OR p.id IN :projectIds)
        AND (:externalId = s.externalId)
        ORDER BY s.name
        """)
    Optional<SiteProjection> findProjectionByExternalId(
        @Param("ignoreProjectFilter") boolean ignoreProjectFilter,
        @Param("projectIds") List<Long> projectIds,
        @Param("externalId") UUID externalId
    );

}
