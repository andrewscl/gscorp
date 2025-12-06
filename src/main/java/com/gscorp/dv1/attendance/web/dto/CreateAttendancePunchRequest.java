package com.gscorp.dv1.attendance.web.dto;

import java.time.OffsetDateTime;

import lombok.Data;

@Data
public class CreateAttendancePunchRequest {
  private OffsetDateTime clientTs;
  private Double lat;
  private Double lon;
  private Double accuracy;
  private Boolean locationOk;
  private String deviceInfo;
  private String ip;
  private String clientTimezone;
  private String timezoneSource;
  private String action;
}