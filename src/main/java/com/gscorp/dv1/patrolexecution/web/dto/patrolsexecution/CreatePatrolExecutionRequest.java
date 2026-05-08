package com.gscorp.dv1.patrolexecution.web.dto.patrolsexecution;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import org.springframework.web.multipart.MultipartFile;

import com.gscorp.dv1.enums.PatrolExecutionStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
@Getter @Setter
public class CreatePatrolExecutionRequest {

    private OffsetDateTime patrolDateTime;
    private PatrolExecutionStatus patrolExecutionStatus;
    private Long userId;
    private Long employeeId;
    private String description;
    private MultipartFile photo;
    private MultipartFile video;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String clientTimeZone;
    private String timezoneSource;

}
