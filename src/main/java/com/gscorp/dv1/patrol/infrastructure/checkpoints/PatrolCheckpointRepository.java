package com.gscorp.dv1.patrol.infrastructure.checkpoints;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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

}
