package com.gscorp.dv1.patrol.infrastructure.patrols;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PatrolRepository extends JpaRepository<Patrol, Long>{

     @Query("""
            SELECT
                p.id            AS id,
                p.externalId    AS externalId,
                p.name          AS name,
                p.description   AS description,
                s.name          AS siteName,
                p.dayFrom       AS dayFrom,
                p.dayTo         AS dayTo,
                p.active        AS active
            FROM Patrol p
            JOIN p.site s
            JOIN s.project pr
            WHERE pr.client.id IN :clientIds
    """)
    List<PatrolProjection> findByClientIdsPatrolProjections(
        @Param("clientIds") List<Long> clientIds);


    @EntityGraph(attributePaths = {"schedules", "checkpoints"})
    @Query ("""
        SELECT
            p.id            AS id,
            p.externalId    AS externalId,
            p.name          AS name,
            p.description   AS description,
            s.name          AS siteName,
            p.dayFrom       AS dayFrom,
            p.dayTo         AS dayTo,
            p.active        AS active
        FROM Patrol p
        JOIN p.site s
        WHERE p.id = :id
    """)
    Optional<PatrolProjection> findProjectionById (
                                    @Param("id") Long id);


    @Query ("""
        SELECT
            p.id            AS id,
            p.externalId    AS externalId,
            p.name          AS name,
            p.description   AS description,
            s.id            AS siteId,
            s.name          AS siteName,
            p.dayFrom       AS dayFrom,
            p.dayTo         AS dayTo,
            p.active        AS active
        FROM Patrol p
        JOIN p.site s
        WHERE p.externalId = :externalId
    """)
    Optional<PatrolProjection> findProjectionByExternalId (
                                    @Param("externalId") UUID externalId);

    @Query ("""
        SELECT p
        FROM Patrol p
        JOIN FETCH p.site s
        WHERE p.externalId = :externalId
    """)
    Optional<Patrol> findByExternalId (
                                    @Param("externalId") UUID externalId);
}
