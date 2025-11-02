package com.gscorp.dv1.attendance.web.dto;

import lombok.Data;

@Data
public class CreateAttendancePunchRequest {
    private Long userId;
    private Double lat;
    private Double lon;
    private Double accuracy;
    private String ip;
    private String deviceInfo;
    private Long siteId;
}