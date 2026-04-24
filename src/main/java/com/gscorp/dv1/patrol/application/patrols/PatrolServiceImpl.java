package com.gscorp.dv1.patrol.application.patrols;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gscorp.dv1.patrol.infrastructure.checkpoints.PatrolCheckpoint;
import com.gscorp.dv1.patrol.infrastructure.checkpoints.PatrolCheckpointProjection;
import com.gscorp.dv1.patrol.infrastructure.checkpoints.PatrolCheckpointRepository;
import com.gscorp.dv1.patrol.infrastructure.patrols.Patrol;
import com.gscorp.dv1.patrol.infrastructure.patrols.PatrolProjection;
import com.gscorp.dv1.patrol.infrastructure.patrols.PatrolRepository;
import com.gscorp.dv1.patrol.infrastructure.schedules.PatrolSchedule;
import com.gscorp.dv1.patrol.infrastructure.schedules.PatrolScheduleProjection;
import com.gscorp.dv1.patrol.infrastructure.schedules.PatrolScheduleRepository;
import com.gscorp.dv1.patrol.web.dto.patrols.CreatePatrolRequest;
import com.gscorp.dv1.patrol.web.dto.patrols.PatrolDto;
import com.gscorp.dv1.patrol.web.dto.patrols.UpdatePatrolRequest;
import com.gscorp.dv1.sites.infrastructure.Site;
import com.gscorp.dv1.sites.infrastructure.SiteRepository;
import com.gscorp.dv1.users.application.UserService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PatrolServiceImpl implements PatrolService {

    private final PatrolRepository patrolRepository;
    private final PatrolScheduleRepository patrolScheduleRepository;
    private final PatrolCheckpointRepository patrolCheckpointRepository;
    private final SiteRepository siteRepository;
    private final UserService userService;

    @Override
    @Transactional(readOnly = true)
    public List<PatrolDto> getPatrolsByUserId(Long userId) {

        List<Long> clientIds = userService.getClientIdsForUser(userId);
        if (clientIds == null || clientIds.isEmpty()) {
            throw new IllegalArgumentException(
                "User with ID " + userId + " is not associated with any clients."
            );
        }

        List<PatrolProjection> patrolProjections = patrolRepository
                                    .findByClientIdsPatrolProjections(clientIds);
        if(patrolProjections == null || patrolProjections.isEmpty()) {
            log.info("No patrols found for user ID: {}", userId);
            return Collections.emptyList();
        }

        List<PatrolDto> patrolDtos = patrolProjections.stream()
                                            .map(PatrolDto::fromProjection)
                                            .toList();

        return patrolDtos;
    }


    @Override
    @Transactional
    public PatrolDto savePatrol (CreatePatrolRequest request, Long userId){

        Site site = siteRepository.getReferenceById(request.siteId());

        Patrol patrol = Patrol.builder()
                                .name(request.name())
                                .description(request.description())
                                .dayFrom(request.dayFrom())
                                .dayTo(request.dayTo())
                                .active(true)
                                .site(site)
                                .createdBy(userId.toString())
                                .updatedBy(userId.toString())
                                .build();

        //Agregar schedules y checkpoints
        if (request.scheduleTimes() != null) {
            request.scheduleTimes().forEach(timeStr -> {
                if (timeStr != null && !timeStr.isBlank()) {
                    patrol.addSchedule(PatrolSchedule.builder()
                        .startTime(LocalTime.parse(timeStr))
                        .active(true)
                        .build());
                }
            });
        }

        if (request.checkpoints() != null) {
            request.checkpoints().forEach(name -> 
                patrol.addCheckpoint(PatrolCheckpoint.builder()
                    .name(name)
                    .latitude(BigDecimal.ZERO)
                    .longitude(BigDecimal.ZERO)
                    .minutesToReach(0)
                    .active(true)
                    .build())
            );
        }

        Patrol saved = patrolRepository.save(patrol);

        PatrolProjection savedProjection = patrolRepository.findProjectionById(saved.getId())
            .orElseThrow(() -> new EntityNotFoundException(
                "Failed to retrieve saved patrol with ID: " + saved.getId()
            ));

        // Get schedules
        List<PatrolScheduleProjection> schedules = patrolScheduleRepository
                .findByPatrolId(savedProjection.getId());

        // Get checkpoints
        List<PatrolCheckpointProjection> checkpoints = patrolCheckpointRepository
                .findByPatrolIdOrderByCheckpointOrderAsc(savedProjection.getId());

        return PatrolDto.fromProjection(savedProjection, schedules, checkpoints);

    }


    @Override
    @Transactional(readOnly = true)
    public PatrolDto getPatrolByExternalId(String externalIdStr) {

        UUID externalId = UUID.fromString(externalIdStr);

        // Get projection
        PatrolProjection patrolProjection = patrolRepository.findProjectionByExternalId(externalId)
                .orElseThrow(() ->
                    new EntityNotFoundException(
                            "No patrol found with external ID: " + externalId)
                );

        // Get schedules
        List<PatrolScheduleProjection> schedules = patrolScheduleRepository
                .findByPatrolId(patrolProjection.getId());

        // Get checkpoints
        List<PatrolCheckpointProjection> checkpoints = patrolCheckpointRepository
                .findByPatrolIdOrderByCheckpointOrderAsc(patrolProjection.getId());

        return PatrolDto.fromProjection(
                            patrolProjection, schedules, checkpoints);
    }


    @Override
    @Transactional
    public PatrolDto updatePatrol(
        String externalIdStr,
        UpdatePatrolRequest request,
        Long userId
    ) {

        UUID externalId = UUID.fromString(externalIdStr);

        Patrol patrol = patrolRepository.findByExternalId(externalId)
                .orElseThrow(() ->
                    new EntityNotFoundException(
                            "No patrol found with external ID: " + externalId)
                );

        //Actualizamos campos básicos
        patrol.setName(request.name());
        patrol.setDescription(request.description());
        patrol.setDayFrom(request.dayFrom());
        patrol.setDayTo(request.dayTo());
        patrol.setActive(request.active());
        patrol.setUpdatedBy(userId.toString());

        //Sincronizar schedules
        if(request.schedules() != null) {
            request.schedules().forEach(dto->{
                //Buscar si existe el horario
                patrolScheduleRepository
                            .findByPatrolIdAndStartTime(patrol.getId(), dto.startTime())
                    .ifPresentOrElse(
                        existing -> existing.setActive(dto.active()),
                        () -> {
                            //Si no existe lo creamos
                            if(Boolean.TRUE.equals(dto.active())){
                                patrolScheduleRepository.save(PatrolSchedule.builder()
                                    .startTime(dto.startTime())
                                    .active(true)
                                    .patrol(patrol)
                                    .build());
                            }
                            
                        }
                    );
            });
        }

        //Sincronizar checkpoints
        if(request.checkpoints() != null) {
            request.checkpoints().forEach(dto->{
                //Comenzar con la caja vacia por defecto
                Optional<PatrolCheckpoint> existingOpt = Optional.empty();
                //intenta buscar el externalID si no es nulo o vacio
                if (dto.externalId() != null || !dto.externalId().isBlank()){
                    try{
                        UUID externalCheckpointId = UUID.fromString(dto.externalId());
                        existingOpt = patrolCheckpointRepository
                                            .findByExternalId(externalCheckpointId);
                    } catch (IllegalArgumentException e) {
                        existingOpt = Optional.empty();
                    }
                }
                existingOpt.ifPresentOrElse(
                    //Actuaizacion
                        existing -> {
                            existing.setName(dto.name());
                            existing.setLatitude(dto.latitude());
                            existing.setLongitude(dto.longitude());
                            existing.setCheckpointOrder(dto.checkpointOrder());
                            existing.setStayTime(dto.stayTime());
                            existing.setMinutesToReach(dto.minutesToReach());
                            existing.setActive(dto.active());
                        },
                        () -> {
                            //Si no existe lo creamos
                            if(Boolean.TRUE.equals(dto.active())){
                                patrolCheckpointRepository.save(PatrolCheckpoint.builder()
                                    .externalId(UUID.randomUUID())
                                    .name(dto.name())
                                    .latitude(dto.latitude())
                                    .longitude(dto.longitude())
                                    .checkpointOrder(dto.checkpointOrder())
                                    .stayTime(dto.stayTime())
                                    .minutesToReach(dto.minutesToReach())
                                    .active(true)
                                    .patrol(patrol)
                                    .build());
                            }
                        }
                    );
            });
        }

        //Guardar
        patrolRepository.save(patrol);

        // Get projection
        PatrolProjection patrolProjection = patrolRepository.findProjectionByExternalId(externalId)
                .orElseThrow(() ->
                    new EntityNotFoundException(
                            "No patrol found with external ID: " + externalId)
                );

        // Get schedules
        List<PatrolScheduleProjection> schedules = patrolScheduleRepository
                .findByPatrolId(patrol.getId());

        // Get checkpoints
        List<PatrolCheckpointProjection> checkpoints = patrolCheckpointRepository
                .findByPatrolIdOrderByCheckpointOrderAsc(patrol.getId());

        return PatrolDto.fromProjection(
                            patrolProjection, schedules, checkpoints);
    }
}
