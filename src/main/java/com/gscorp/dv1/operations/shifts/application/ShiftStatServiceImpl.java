package com.gscorp.dv1.operations.shifts.application;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.gscorp.dv1.components.ZoneResolver;
import com.gscorp.dv1.components.dto.ZoneResolutionResult;
import com.gscorp.dv1.operations.shifts.infrastructure.ShiftRepository;
import com.gscorp.dv1.operations.shifts.infrastructure.projections.ProjectSiteShiftsSummaryProjection;
import com.gscorp.dv1.operations.shifts.web.dto.ShiftsCountLast24HoursDto;
import com.gscorp.dv1.operations.shifts.web.dto.statistics.ProjectSiteShiftsSummaryDto;
import com.gscorp.dv1.users.application.UserScopeService;
import com.gscorp.dv1.users.application.dto.ProjectScope;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ShiftStatServiceImpl implements ShiftStatService {

    private final UserScopeService userScopeService;
    private final ShiftRepository shiftRepository;
    private final ZoneResolver zoneResolver;
    
    @Transactional(readOnly = true)
    public List<ProjectSiteShiftsSummaryDto> getLast24hoursProjectSiteShiftsSummary (
                                                    UUID userExternalId,
                                                    String zoneIdStr){
        if (userExternalId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado");
        }
        ProjectScope scope = userScopeService.getProjectScope();
        ZoneResolutionResult zoneResult = zoneResolver.resolveZone(userExternalId, zoneIdStr);
        ZoneId zoneId = zoneResult.zoneId();
        OffsetDateTime fromTs = OffsetDateTime.now(zoneId).minusHours(24);
        OffsetDateTime toTs = OffsetDateTime.now(zoneId);
        List<ProjectSiteShiftsSummaryProjection> projections =
                shiftRepository.findProjectSiteShiftsLast24HoursSummaryByProjectIds(
                                scope.ignoreFilter(), 
                                scope.projectIds(), 
                                fromTs, toTs, 
                                null,
                                null, 
                                null,
                                null);
        return projections.stream()
                    .map(ProjectSiteShiftsSummaryDto::fromProjection)
                    .toList();
    }

    @Transactional(readOnly = true)
    public List<ShiftsCountLast24HoursDto> getShiftsCountLast24Hours(
                                                UUID userExternalId){
        if (userExternalId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado");
        }
        ProjectScope scope = userScopeService.getProjectScope();
        OffsetDateTime since = OffsetDateTime.now().minusHours(24);
        OffsetDateTime until = OffsetDateTime.now();
        var projections = shiftRepository.getShiftsCountLast24Hours(scope.ignoreFilter(), scope.projectIds(), since, until);
        System.out.println("====== PROBANDO ENDPOINT LAST 24 HOURS ======");
        System.out.println("Cantidad de registros devueltos: " + (projections != null ? projections.size() : "NULL"));
        if (projections != null) {
            projections.forEach(row -> {
                System.out.println("Fecha/Hora: " + row.getStartTs() + " | Turnos: " + row.getTotalShifts());
            });
        }
        System.out.println("=============================================");
        if (projections == null) {
            return List.of(); 
        }
        return projections.stream()
                    .map(ShiftsCountLast24HoursDto::fromProjection)
                    .toList();
    }
}
