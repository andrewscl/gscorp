package com.gscorp.dv1.attendance.application;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gscorp.dv1.admin.clients.application.ClientService;
import com.gscorp.dv1.attendance.infrastructure.AttendancePunchRepo;
import com.gscorp.dv1.attendance.infrastructure.projections.statistics.ProjectSiteAttendancesSummaryProjection;
import com.gscorp.dv1.attendance.web.dto.statistics.ProjectSiteAttendancesSummaryDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttendanceStatServiceImpl implements AttendanceStatService{

    private final ClientService clientService;
    private final AttendancePunchRepo attendancePunchRepo;

    @Transactional(readOnly = true)
    public List<ProjectSiteAttendancesSummaryDto> getProjectSiteAttendancesTodaySummaryByUserExternalId(UUID userExternalId){
    
        List<Long> clientIds = clientService.getClientIdsByUserExternalId(userExternalId);
        if (clientIds == null || clientIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<ProjectSiteAttendancesSummaryProjection> projections =
                attendancePunchRepo.getDailyProjectSiteAttendancesSummaryByClients(clientIds);

        return projections.stream()
                .map(ProjectSiteAttendancesSummaryDto::fromProjection)
                .toList();
    }
    
}
