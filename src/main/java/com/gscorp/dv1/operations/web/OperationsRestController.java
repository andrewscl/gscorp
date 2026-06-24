package com.gscorp.dv1.operations.web;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.gscorp.dv1.attendance.application.AttendanceStatService;
import com.gscorp.dv1.operations.web.dto.OperationsDashboardResponse;
import com.gscorp.dv1.security.SecurityUser;
import com.gscorp.dv1.shiftrequests.application.ShiftRequestStatService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/operations")
@RequiredArgsConstructor
public class OperationsRestController {

    private final ShiftRequestStatService shiftRequestStatService;
    private final AttendanceStatService attendanceStatService;

    @GetMapping("/ops-dashboard-metrics")
    public OperationsDashboardResponse getOperationsDashboard (
                    @AuthenticationPrincipal SecurityUser securityUser
    ){

        if (securityUser == null) {
            log.warn("Intento de acceso no autenticado");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado.");
        }

        UUID userExternalId = securityUser.getUser().getExternalId();

        OperationsDashboardResponse metrics =
            new OperationsDashboardResponse(
                shiftRequestStatService
                    .getProjectSiteShiftRequestsSummaryTodaySummaryByUserExternalId(userExternalId),
                attendanceStatService
                    .getProjectSiteAttendancesTodaySummaryByUserExternalId(userExternalId)
            );
        return metrics;
    }

}
