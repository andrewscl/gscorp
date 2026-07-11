package com.gscorp.dv1.operations.patrol.application.schedules;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gscorp.dv1.operations.patrol.infrastructure.schedules.PatrolSchedule;
import com.gscorp.dv1.operations.patrol.infrastructure.schedules.PatrolScheduleRepository;
import com.gscorp.dv1.operations.patrol.web.schedules.dto.PatrolScheduleDto;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PatrolScheduleServiceImpl implements PatrolScheduleService {

    private final PatrolScheduleRepository patrolScheduleRepo;

    @Transactional(readOnly = true)
    public List<PatrolScheduleDto> getNext24hPatrolSchedulesBySiteExternalId (
                                        UUID siteExternalId) {

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime target = now.plusHours(24);

        LocalTime nowTime = now.toLocalTime();
        LocalTime targetTime = target.toLocalTime();

        Integer currentDayOfWeek = now.getDayOfWeek().getValue();
        Integer nextDay = target.getDayOfWeek().getValue();

        boolean isOvernight =
            targetTime.isBefore(nowTime) ||
            now.toLocalDate().isBefore(target.toLocalDate());

        List<PatrolSchedule> schedules = 
            patrolScheduleRepo.findNext24hSchedulesBySiteExternalId(
                    siteExternalId,
                    nowTime,
                    targetTime,
                    currentDayOfWeek,
                    nextDay,
                    isOvernight);

        // 🟢 Re-ordena la lista para turnos nocturnos
        if (isOvernight) {
            schedules = schedules.stream()
                .sorted((s1, s2) -> {
                    // Si una hora es mayor o igual a "ahora", pertenece al bloque de hoy (ej: 23:00)
                    // Si es menor, pertenece al bloque de la madrugada de mañana (ej: 02:00)
                    boolean s1IsToday = !s1.getStartTime().isBefore(nowTime);
                    boolean s2IsToday = !s2.getStartTime().isBefore(nowTime);

                    if (s1IsToday && !s2IsToday) return -1; // s1 va primero (es de hoy)
                    if (!s1IsToday && s2IsToday) return 1;  // s2 va primero (es de hoy)
                    
                    // Si ambas son del mismo bloque (ambas de hoy o ambas de mañana), comparamos normal
                    return s1.getStartTime().compareTo(s2.getStartTime());
                })
                .toList();
        }

        return schedules.stream()
                        .map(PatrolScheduleDto::fromEntity)
                        .toList();
    }


    @Transactional(readOnly = true)
    public PatrolScheduleDto getPatrolExternalIdByScheduleExternalId(UUID scheduleExternalId){

        return patrolScheduleRepo.getPatrolExternalIdByScheduleExternalId(scheduleExternalId)
                .map(PatrolScheduleDto::fromProjection)
                .orElseThrow(() ->
                        new EntityNotFoundException("No se encontró el patrolId con scheduleId: " + scheduleExternalId));

    }

}
