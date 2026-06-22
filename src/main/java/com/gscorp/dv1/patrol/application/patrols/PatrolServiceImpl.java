package com.gscorp.dv1.patrol.application.patrols;

import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.gscorp.dv1.clients.application.ClientService;
import com.gscorp.dv1.enums.PatrolScheduleStatus;
import com.gscorp.dv1.patrol.infrastructure.checkpoints.PatrolCheckpoint;
import com.gscorp.dv1.patrol.infrastructure.checkpoints.PatrolCheckpointProjection;
import com.gscorp.dv1.patrol.infrastructure.checkpoints.PatrolCheckpointRepository;
import com.gscorp.dv1.patrol.infrastructure.patrols.Patrol;
import com.gscorp.dv1.patrol.infrastructure.patrols.PatrolProjection;
import com.gscorp.dv1.patrol.infrastructure.patrols.PatrolRepository;
import com.gscorp.dv1.patrol.infrastructure.schedules.PatrolSchedule;
import com.gscorp.dv1.patrol.infrastructure.schedules.PatrolScheduleProjection;
import com.gscorp.dv1.patrol.infrastructure.schedules.PatrolScheduleRepository;
import com.gscorp.dv1.patrol.web.patrols.dto.CreatePatrolRequest;
import com.gscorp.dv1.patrol.web.patrols.dto.PatrolDto;
import com.gscorp.dv1.patrol.web.patrols.dto.UpdatePatrolRequest;
import com.gscorp.dv1.sites.infrastructure.Site;
import com.gscorp.dv1.sites.infrastructure.SiteRepository;

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
    private final ClientService clientService;
    private final SiteRepository siteRepository;

    @Override
    @Transactional(readOnly = true)
    public List<PatrolDto> getPatrolsByUserExternalUserId(UUID userExternalId) {

        List<Long> clientIds = clientService.getClientIdsByUserExternalId(userExternalId);
        if (clientIds == null || clientIds.isEmpty()) {
            throw new IllegalArgumentException(
                "User with ID " + userExternalId + " is not associated with any clients."
            );
        }

        List<PatrolProjection> patrolProjections = patrolRepository
                                    .findByClientIdsPatrolProjections(clientIds);
        if(patrolProjections == null || patrolProjections.isEmpty()) {
            log.info("No patrols found for user ID: {}", userExternalId);
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

        System.out.println("====== DIAGNÓSTICO ======");
        System.out.println("Objeto Request completo: " + request);
        System.out.println("¿scheduleTimes es null?: " + (request.scheduleTimes() == null));
        if (request.scheduleTimes() != null) {
            System.out.println("Tamaño de scheduleTimes: " + request.scheduleTimes().size());
        }
        System.out.println("=========================");

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

        Patrol saved = patrolRepository.save(patrol);

        // 3. 🟢 SOLUCIÓN: Guardar físicamente los horarios usando su propio repositorio
        if (request.scheduleTimes() != null) {
            request.scheduleTimes().forEach(timeStr -> {
                if (timeStr != null && !timeStr.isBlank()) {
                    
                    PatrolSchedule schedule = PatrolSchedule.builder()
                        .externalId(UUID.randomUUID()) // Asegúrate de darle un ID único si lo maneja tu entidad
                        .startTime(LocalTime.parse(timeStr))
                        .active(true)
                        .status(PatrolScheduleStatus.SCHEDULED)
                        .patrol(saved) // 🔴 CRUCIAL: Vinculamos el objeto 'saved' que ya tiene ID de la BD
                        .build();
                    
                    // Forzamos el INSERT físico en la tabla patrol_schedules
                    patrolScheduleRepository.save(schedule);
                }
            });
        }

        PatrolProjection savedProjection = patrolRepository
                                .findProjectionById(saved.getId())
            .orElseThrow(() ->
                new EntityNotFoundException(
                "Failed to retrieve saved patrol with ID: "
                + saved.getId()
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

                // Eliminar checkpoint
                if(Boolean.TRUE.equals(dto.deleted())) {
                    if(StringUtils.hasText(dto.externalId())) {
                        UUID deleteId = UUID.fromString(dto.externalId());
                        patrolCheckpointRepository.deleteByExternalId(deleteId);
                    }
                    return;
                }

                //Comenzar con la caja vacia por defecto
                Optional<PatrolCheckpoint> existingOpt = Optional.empty();

                // Actualizar checkpoint
                if (StringUtils.hasText(dto.externalId())){
                    try{
                        UUID externalCheckpointId = UUID.fromString(dto.externalId());
                        existingOpt = patrolCheckpointRepository
                                            .findByExternalId(externalCheckpointId);
                    } catch (IllegalArgumentException e) {
                        existingOpt = Optional.empty();
                    }
                }
                existingOpt.ifPresentOrElse(
                        existing -> {
                            // Actualizar datos existentes
                            existing.setName(dto.name());
                            existing.setDescription(dto.description());
                            existing.setLatitude(dto.latitude());
                            existing.setLongitude(dto.longitude());
                            existing.setCheckpointOrder(dto.checkpointOrder());
                            existing.setStayTime(dto.stayTime());
                            existing.setMinutesToReach(dto.minutesToReach());
                            existing.setActive(dto.active());
                        },
                        () -> {
                            // Crear nuevo solo sino esta marcado para borrar y no existe
                            if(dto.name()!= null && !dto.name().isBlank()){
                                patrolCheckpointRepository.save(PatrolCheckpoint.builder()
                                    .externalId(UUID.randomUUID())
                                    .name(dto.name())
                                    .description(dto.description())
                                    .latitude(dto.latitude())
                                    .longitude(dto.longitude())
                                    .checkpointOrder(dto.checkpointOrder())
                                    .stayTime(dto.stayTime())
                                    .minutesToReach(dto.minutesToReach())
                                    .active(dto.active() != null ? dto.active() : true)
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
