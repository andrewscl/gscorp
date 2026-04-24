package com.gscorp.dv1.patrol.application.checkpoints;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gscorp.dv1.patrol.infrastructure.checkpoints.PatrolCheckpointRepository;
import com.gscorp.dv1.patrol.infrastructure.patrols.Patrol;
import com.gscorp.dv1.patrol.infrastructure.patrols.PatrolRepository;
import com.gscorp.dv1.patrol.web.dto.checkpoints.PatrolCheckpointDto;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PatrolCheckpointServiceImpl implements PatrolCheckpointService {

    private final PatrolRepository patrolRepo;
    private final PatrolCheckpointRepository patrolCheckpointRepo;

    @Override
    @Transactional(readOnly = true)
    public List<PatrolCheckpointDto> getCheckpointsByExternalId (UUID externalId) {

        //Buscar los puntos ordenados
        Patrol patrol = patrolRepo.findByExternalId(externalId)
            .orElseThrow(() ->
                new EntityNotFoundException("Ronda no encontrada"));

        List<PatrolCheckpointDto> checkpoints = 
            patrolCheckpointRepo
                .findByPatrolIdOrderByCheckpointOrderAsc(patrol.getId())
                    .stream()
                    .map(PatrolCheckpointDto::fromProjection)
                    .toList();

        return checkpoints;
    }

}
