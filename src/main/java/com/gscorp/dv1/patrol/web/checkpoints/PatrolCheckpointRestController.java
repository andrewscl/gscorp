package com.gscorp.dv1.patrol.web.checkpoints;

import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gscorp.dv1.patrol.application.checkpoints.PatrolCheckpointService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/patrol-chekpoints")
@RequiredArgsConstructor
public class PatrolCheckpointRestController {

    private final PatrolCheckpointService checkpoints;

    @GetMapping("/{externalId}")
    public ResponseEntity<?> getPatrolCheckpoints(
            @PathVariable UUID patrolExternalId
    ) {
        
        try {
            return ResponseEntity
                .ok(checkpoints.getCheckpointsByPatrolExternalId(patrolExternalId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", "Error al buscar patrol checkpoints"));
        }
    }

}
