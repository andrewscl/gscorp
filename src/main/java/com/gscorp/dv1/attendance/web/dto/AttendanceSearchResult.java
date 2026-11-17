package com.gscorp.dv1.attendance.web.dto;

import java.time.LocalDate;

import org.springframework.data.domain.Page;

public record AttendanceSearchResult (
    Page<AttendancePunchDto> punchsPage,
    LocalDate from,
    LocalDate to
){
    
}
