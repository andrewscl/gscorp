package com.gscorp.dv1.attendance.application;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import com.gscorp.dv1.attendance.infrastructure.AttendancePunch;
import com.gscorp.dv1.attendance.infrastructure.AttendancePunchRepo;
import com.gscorp.dv1.attendance.web.dto.CreateAttendancePunchRequest;
import com.gscorp.dv1.attendance.web.dto.HourlyCountDto;
import com.gscorp.dv1.sites.infrastructure.Site;

public interface AttendanceService {
    
    Site findNearestSite(double lat, double lon);
    AttendancePunch punch(Long userId, double lat, double lon,
                            Double acc, String ip, String ua, Site site);
    Optional<AttendancePunch> lastPunch(Long userId);
    List<AttendancePunchRepo.DayCount> seriesByUser(Long userId, LocalDate from, LocalDate to, String action);
    List<AttendancePunch> listForUser(Long userId, LocalDate from, LocalDate to, ZoneId zone);
    double haversineMeters(double lat1,double lon1,double lat2,double lon2);
    long countByClientIdAndDate(Long clientId, LocalDate date);
    AttendancePunch punch(CreateAttendancePunchRequest dto);
    List<HourlyCountDto> getHourlyCounts(LocalDate date, String tz, String action, Long userId);
    long countByClientIdsAndDate(List<Long> clientIds, LocalDate date, String action, String tz);

}
