package com.gscorp.dv1.patrol.application;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gscorp.dv1.patrol.infrastructure.PatrolProjection;
import com.gscorp.dv1.patrol.infrastructure.PatrolRepository;
import com.gscorp.dv1.patrol.web.dto.PatrolDto;
import com.gscorp.dv1.users.application.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PatrolServiceImpl implements PatrolService {

    private final PatrolRepository patrolRepository;
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
                                    patrolRepository.findByClientIdsPatrolProjections(clientIds);
        if(patrolProjections == null || patrolProjections.isEmpty()) {
            log.info("No patrols found for user ID: {}", userId);
            return Collections.emptyList();
        }

        List<PatrolDto> patrolDtos = patrolProjections.stream()
                                            .map(PatrolDto::fromProjection)
                                            .toList();

         return patrolDtos;
    }

}
