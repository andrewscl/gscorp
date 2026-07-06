package com.gscorp.dv1.attendance.application;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;

import com.gscorp.dv1.attendance.infrastructure.AttendancePunch;
import com.gscorp.dv1.attendance.infrastructure.AttendancePunchRepo;
import com.gscorp.dv1.attendance.web.dto.AttendancePunchDto;
import com.gscorp.dv1.attendance.web.dto.AttendancePunchPointDto;
import com.gscorp.dv1.attendance.web.dto.CreateAttendancePunchRequest;
import com.gscorp.dv1.attendance.web.dto.HourlyCountDto;
import com.gscorp.dv1.attendance.web.dto.DashboardHeaderInfo;

public interface AttendanceService {


    AttendancePunchDto createPunch (
        CreateAttendancePunchRequest req, UUID userExternalId
    );


    Optional<AttendancePunch> lastPunch(Long userId);


    List<AttendancePunchRepo.DayCount> seriesByUser(
                    Long userId, LocalDate from, LocalDate to, String action);


    long countByClientIdAndDate(Long clientId, LocalDate date);


    List<HourlyCountDto> getHourlyCounts(LocalDate date, String tz, String action, Long userId);


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


    List<HourlyCountDto> getAttendanceSeriesForUserByHours(
        UUID userExternalId,
        LocalDate date,
        ZoneId zone,
        String action,
        Long siteId,
        Long projectId
    );

    DashboardHeaderInfo getDashboardHeader (UUID userExternalId);

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


}
