package com.gscorp.dv1.shiftrequests.application;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gscorp.dv1.clients.application.ClientService;
import com.gscorp.dv1.shiftrequests.infrastructure.ShiftRequestScheduleRepository;
import com.gscorp.dv1.shiftrequests.infrastructure.projections.statistics.ProjectSiteShiftRequestSchedulesProjection;
import com.gscorp.dv1.shiftrequests.web.dto.statistics.ProjectSiteShiftRequestsSchedulesDto;
import com.gscorp.dv1.shiftrequests.web.dto.statistics.ProjectSiteShiftRequestsSummaryDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShiftRequestStatServiceImpl implements ShiftRequestStatService{

    private final ClientService clientService;
    private final ShiftRequestScheduleRepository scheduleRepository;

    @Transactional(readOnly = true)
    public List<ProjectSiteShiftRequestsSummaryDto>
                getProjectSiteShiftRequestsSummaryTodaySummaryByUserExternalId(
                                                                UUID userExternalId){

        List<Long> clientIds = clientService.getClientIdsByUserExternalId(userExternalId);
        if (clientIds == null || clientIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<ProjectSiteShiftRequestSchedulesProjection> projections =
                scheduleRepository.findProjectSiteShiftRequestSchedulesByClientIds(clientIds);

        List<ProjectSiteShiftRequestsSchedulesDto> schedules =
                                projections.stream()
                                            .map(ProjectSiteShiftRequestsSchedulesDto::fromProjection)
                                            .toList();

        LocalDate today = LocalDate.now();
        DayOfWeek todayEnum = today.getDayOfWeek();

        Map<String, Long> countsByProjectSite = new HashMap<>();
        Map<String, ProjectSiteShiftRequestsSchedulesDto> metadataMap = new HashMap<>();

        for (ProjectSiteShiftRequestsSchedulesDto sc : schedules) {
            
            // 1. Validar vigencia de fechas del contrato/requerimiento
            if (sc.requestStartDate() != null && today.isBefore(sc.requestStartDate())) continue;
            if (sc.requestEndDate() != null && today.isAfter(sc.requestEndDate())) continue;

            // 2. Validar si el día de hoy aplica en el rango usando el Forecast Helper
            Set<DayOfWeek> allowedDays = 
                    ShiftRequestForecastHelper.buildDaySet(sc.dayFrom(), sc.dayTo());

            // Comparación por número posicional del día (1..7) para evitar conflictos de paquetes
            boolean dayMatches = allowedDays.contains(todayEnum);

            if (dayMatches) {
                // Llave compuesta única para no mezclar sitios con nombres idénticos en proyectos distintos
                String uniqueKey = sc.projectId() + "_" + sc.siteId();
                
                countsByProjectSite.put(uniqueKey, countsByProjectSite.getOrDefault(uniqueKey, 0L) + 1);
                metadataMap.putIfAbsent(uniqueKey, sc);
            }
        }

        return metadataMap.entrySet().stream()
                .map(entry -> {
                    String key = entry.getKey();
                    ProjectSiteShiftRequestsSchedulesDto meta = entry.getValue();
                    Long totalShiftsToday = countsByProjectSite.getOrDefault(key, 0L);

                    return new ProjectSiteShiftRequestsSummaryDto(
                        meta.projectId(),
                        meta.projectName(),
                        meta.siteId(),
                        meta.siteName(),
                        totalShiftsToday
                    );
                })
                .sorted(Comparator.comparing(ProjectSiteShiftRequestsSummaryDto::projectName)
                        .thenComparing(ProjectSiteShiftRequestsSummaryDto::siteName))
                .toList();

    }

}
