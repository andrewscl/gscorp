package com.gscorp.dv1.patrol.web;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gscorp.dv1.patrol.application.PatrolRunService;
import com.gscorp.dv1.patrol.web.dto.PatrolHourlyDto;
import com.gscorp.dv1.users.application.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/patrol-runs")
@RequiredArgsConstructor
public class PatrolRunRestController {

    private final PatrolRunService patrolRunService;
    private final UserService userService;

  /**
   * GET /api/patrols-runs/hourly
   *
   * Query params:
   *  - clientId (optional, multiple) : List of client ids. Non-admins may only request their own clients.
   *  - date (required, ISO date) : date to aggregate (local day in tz)
   *  - tz   (optional, default "UTC") : timezone id used to bucket hours
   *
   * Behavior:
   *  - If clientId provided:
   *      - if caller is admin -> allowed
   *      - else verify caller's clientIds contain all requested clientIds, otherwise 403
   *  - If clientId NOT provided:
   *      - if caller is admin -> 400 Bad Request (policy: admin must provide clientId filter)
   *      - else resolve caller's clientIds via userService.getClientIdsForUser(...)
   *
   * Returns a list of PatrolHourlyDto (patrol/site id, name, hour, count).
   */
  @GetMapping("/hourly")
  public ResponseEntity<List<PatrolHourlyDto>> hourly(
      Authentication auth,
      @RequestParam(required = false) List<Long> clientId,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
      @RequestParam(required = false, defaultValue = "UTC") String tz
  ) {
    boolean isAdmin = userService.isAdmin(auth);
    List<Long> effectiveClientIds;

    if (clientId != null && !clientId.isEmpty()) {
      if (!isAdmin) {
        Long callerUserId = userService.getUserIdFromAuthentication(auth);
        List<Long> userClients = userService.getClientIdsForUser(callerUserId);
        if (userClients == null || !userClients.containsAll(clientId)) {
          log.warn("User {} attempted to access clients {} but only has {}", callerUserId, clientId, userClients);
          return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
      }
      effectiveClientIds = clientId;
    } else {
      if (isAdmin) {
        // Policy: require admin to explicitly pass clientId(s) to avoid accidental wide queries.
        log.warn("Admin requested /patrols-runs/hourly without clientId");
        return ResponseEntity.badRequest().build();
      } else {
        Long callerUserId = userService.getUserIdFromAuthentication(auth);
        effectiveClientIds = userService.getClientIdsForUser(callerUserId);
        if (effectiveClientIds == null || effectiveClientIds.isEmpty()) {
          log.warn("No clientIds found for userId={}", callerUserId);
          return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
      }
    }

    List<PatrolHourlyDto> out = patrolRunService.getPatrolHourlyCounts(effectiveClientIds, date, tz);
    return ResponseEntity.ok(out == null ? Collections.emptyList() : out);
  }

}
