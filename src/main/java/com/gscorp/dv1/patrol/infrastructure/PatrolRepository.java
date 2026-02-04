package com.gscorp.dv1.patrol.infrastructure;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PatrolRepository extends JpaRepository<Patrol, Long>{

     @Query("""
            SELECT
                p.id            AS id,
                p.name          AS name,
                p.description   AS description,
                s.name          AS siteName,
                p.dayFrom       AS dayFrom,
                p.dayTo         AS dayTo,
                p.startTime     AS startTime
            FROM Patrol p
            JOIN p.site s
            JOIN s.project pr
            WHERE pr.client.id IN :clientIds
    """)
    List<PatrolProjection> findByClientIdsPatrolProjections(
        @Param("clientIds") List<Long> clientIds);



    @Query ("""
        SELECT
            p.id            AS id,
            p.name          AS name,
            s.name          AS siteName,
            p.dayFrom       AS dayFrom,
            p.dayTo         AS dayTo
        FROM Patrol p
        JOIN p.site s
        WHERE p.id = :id
    """)
    Optional<PatrolProjection> findProjectionById (@Param("id") Long id);

}
