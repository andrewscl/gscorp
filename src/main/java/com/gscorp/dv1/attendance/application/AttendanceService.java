package com.gscorp.dv1.attendance.application;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;

import com.gscorp.dv1.attendance.infrastructure.AttendancePunch;
import com.gscorp.dv1.attendance.infrastructure.AttendancePunchRepo;
import com.gscorp.dv1.attendance.web.dto.AttendanceSearchResult;
import com.gscorp.dv1.attendance.web.dto.AttendancePunchDto;
import com.gscorp.dv1.attendance.web.dto.CreateAttendancePunchRequest;
import com.gscorp.dv1.attendance.web.dto.dashboard.AttendanceCardState;
import com.gscorp.dv1.attendance.web.dto.dashboard.DashboardHeaderInfo;
import com.gscorp.dv1.attendance.web.dto.statistics.AttendancePunchPointDto;
import com.gscorp.dv1.attendance.web.dto.statistics.AttendancesHourlyCountDto;

public interface AttendanceService {

    AttendancePunchDto createPunch (
                                UUID userExternalId,
                                CreateAttendancePunchRequest req
    );

    Optional<AttendancePunch> lastPunch(Long userId);

    List<AttendancePunchRepo.DayCount> seriesByUser(
                    Long userId, LocalDate from, LocalDate to, String action);

    long countByClientIdAndDate(Long clientId, LocalDate date);

    List<AttendancesHourlyCountDto> getHourlyCounts(
                                LocalDate date,
                                String tz,
                                String action,
                                Long userId);

    long countByClientIdsAndDate(List<Long> clientIds, LocalDate date, String action, String tz);

    List<AttendancePunchDto> findByUserAndDateBetween(
        UUID userExternalId,
        LocalDate fromDate,
        LocalDate toDate,
        String clientTz,
        Long siteId,
        Long projectId,
        String action
    );

    List<AttendancePunchPointDto> getAttendanceSeriesForUserByDates(
        UUID userExternalId,
        LocalDate fromDate,
        LocalDate toDate,
        ZoneId zone,
        String action,
        Long siteId,
        Long projectId
    );

    List<AttendancesHourlyCountDto> getAttendanceSeriesForUserByHours(
        UUID userExternalId,
        LocalDate date,
        ZoneId zone,
        String action,
        Long siteId,
        Long projectId
    );

    DashboardHeaderInfo getDashboardHeader (UUID userExternalId, String tz);

    Page<AttendancePunchDto> getAttendanceTable(
        UUID userExternalId,
        ZoneId zoneId,
        LocalDate fromDate,
        LocalDate toDate,
        Long siteId,
        Long projectId,
        String action,
        int page,
        int size
    );

    String normalizeAction(String a);


    AttendanceSearchResult getByUserExternalIdAndDateBetween(
        UUID userExternalId,
        LocalDate fromDate,
        LocalDate toDate,
        String clientTz,
        int page,
        int size
    );

    AttendanceCardState getAttendanceCardState(
                                UUID userExternalId,
                                String tz);

}
