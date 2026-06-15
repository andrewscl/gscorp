package com.gscorp.dv1.patrol.application.schedules;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gscorp.dv1.patrol.infrastructure.schedules.PatrolSchedule;
import com.gscorp.dv1.patrol.infrastructure.schedules.PatrolScheduleRepository;
import com.gscorp.dv1.patrol.web.dto.schedules.PatrolScheduleDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PatrolScheduleServiceImpl implements PatrolScheduleService {

    private final PatrolScheduleRepository patrolScheduleRepo;

    @Override
    @Transactional(readOnly = true)
    public List<PatrolScheduleDto> getTodaySchedulesBySiteExternalId (
                                        UUID siteExternalId) {

        int currentDayOfWeek = LocalDate.now().getDayOfWeek().getValue();

        List<PatrolSchedule> schedules = 
            patrolScheduleRepo.findTodaySchedulesBySiteExternalId(
                    siteExternalId, currentDayOfWeek);

        return schedules.stream()
                        .map(PatrolScheduleDto::fromEntity)
                        .toList();
    }

}
