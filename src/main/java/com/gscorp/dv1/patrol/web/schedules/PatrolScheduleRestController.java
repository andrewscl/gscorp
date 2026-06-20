package com.gscorp.dv1.patrol.web.schedules;

import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gscorp.dv1.patrol.application.schedules.PatrolScheduleService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/patrol-schedules")
@RequiredArgsConstructor
public class PatrolScheduleRestController {

    private final PatrolScheduleService patrolScheduleService;
    
    @GetMapping("/next-24h-site-patrol-schedules/{siteExternalId}")
    public ResponseEntity<?> getPatrolSchedulesBySiteExternalId (
                        @PathVariable UUID siteExternalId) {

        try {
            return ResponseEntity
                        .ok(patrolScheduleService
                                .getNext24hPatrolSchedulesBySiteExternalId(siteExternalId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", "Error al buscar PatrolSchedules"));
        }

    }

}
