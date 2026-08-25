package com.gscorp.dv1.operations.dashboard.web;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.gscorp.dv1.attendance.application.AttendanceStatService;
import com.gscorp.dv1.config.security.SecurityUser;
import com.gscorp.dv1.operations.dashboard.web.dto.OperationsDashboardResponse;
import com.gscorp.dv1.operations.shiftrequests.application.ShiftRequestStatService;
import com.gscorp.dv1.operations.shifts.application.ShiftStatService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/operations")
@RequiredArgsConstructor
public class OperationsRestController {

    private final ShiftRequestStatService shiftRequestStatService;
    private final AttendanceStatService attendanceStatService;
    private final ShiftStatService shiftStatService;

    @GetMapping("/ops-dashboard-metrics")
    public OperationsDashboardResponse getOperationsDashboard (
                    @AuthenticationPrincipal SecurityUser securityUser,
                    @RequestParam(required = false) String zoneIdStr
    ){
        if (securityUser == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado.");
        UUID userExternalId = securityUser.getUser().getExternalId();
        OperationsDashboardResponse metrics = new OperationsDashboardResponse(
                shiftRequestStatService
                    .getProjectSiteShiftRequestsSummaryTodaySummaryByUserExternalId(userExternalId),
                attendanceStatService
                    .getProjectSiteAttendancesTodaySummaryByUserExternalId(userExternalId),
                shiftStatService
                    .getLast24hoursProjectSiteShiftsSummary(userExternalId, zoneIdStr),
                shiftStatService.getShiftsCountLast24Hours(userExternalId)
            );
        return metrics;
    }

}
