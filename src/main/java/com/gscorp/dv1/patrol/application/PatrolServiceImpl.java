package com.gscorp.dv1.patrol.application;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gscorp.dv1.patrol.infrastructure.Patrol;
import com.gscorp.dv1.patrol.infrastructure.PatrolCheckpoint;
import com.gscorp.dv1.patrol.infrastructure.PatrolProjection;
import com.gscorp.dv1.patrol.infrastructure.PatrolRepository;
import com.gscorp.dv1.patrol.infrastructure.PatrolSchedule;
import com.gscorp.dv1.patrol.web.dto.CreatePatrolRequest;
import com.gscorp.dv1.patrol.web.dto.PatrolDto;
import com.gscorp.dv1.sites.infrastructure.Site;
import com.gscorp.dv1.sites.infrastructure.SiteRepository;
import com.gscorp.dv1.users.application.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PatrolServiceImpl implements PatrolService {

    private final PatrolRepository patrolRepository;
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

        List<PatrolProjection> patrolProjections =
                                    patrolRepository
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

        return patrolRepository.findProjectionById(saved.getId())
                .map(PatrolDto::fromProjection)
                .orElseThrow(() ->
                    new IllegalStateException(
                            "Patrol not found after saving with ID: "
                                 + saved.getId())
                );

    }

}
