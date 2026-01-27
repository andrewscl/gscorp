package com.gscorp.dv1.patrol.infrastructure;

import java.util.List;

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
                p.site.name     AS siteName,
                p.dayFrom       AS dayFrom,
                p.dayTo         AS dayTo,
                p.startTime     AS startTime
            FROM patrol p
            JOIN FETCH p.site s
            JOIN s.project pr
            WHERE pr.client.id IN :clientIds
    """)
    List<PatrolProjection> findAllPatrolProjections(@Param("clientIds") List<Long> clientIds);

}
