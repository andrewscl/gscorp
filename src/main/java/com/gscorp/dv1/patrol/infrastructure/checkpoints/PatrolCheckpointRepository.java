package com.gscorp.dv1.patrol.infrastructure.checkpoints;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface PatrolCheckpointRepository 
            extends JpaRepository<PatrolCheckpoint, Long> {

    /**
     * Busca todos los puntos de control asociados a una ronda específica.
     * @param patrolId ID interno de la ronda.
     * @return Lista de puntos de control.
     */
    List<PatrolCheckpointProjection>
                        findByPatrolIdOrderByCheckpointOrderAsc(Long patrolId);

    /**
     * Opcional: Si necesitas solo los activos para el formulario.
     */
    List<PatrolCheckpointProjection> findByPatrolIdAndActiveTrue(Long patrolId);

    Optional<PatrolCheckpoint> findByPatrolIdAndName(
                                Long patrolId, String name);

    Optional<PatrolCheckpoint>
                        findByExternalId(UUID externalId);

    @Modifying
    @Transactional
    @Query("DELETE FROM PatrolCheckpoint p WHERE p.externalId = :externalId")
    void deleteByExternalId(UUID externalId);

}
