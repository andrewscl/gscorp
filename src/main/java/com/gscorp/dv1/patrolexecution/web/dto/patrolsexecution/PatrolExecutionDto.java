package com.gscorp.dv1.patrolexecution.web.dto.patrolsexecution;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.gscorp.dv1.enums.PatrolExecutionStatus;
import com.gscorp.dv1.patrolexecution.infrastructure.patrolsexecution.PatrolExecution;

public record PatrolExecutionDto (
    Long id,
    UUID externaId,
    Instant startTime,
    Instant endTime,
    PatrolExecutionStatus status,
    Long userId,
    Long employeeId,
    String description,
    String photoPath,
    String videoPath,
    BigDecimal latitude,
    BigDecimal longitude,
    String clientTimezone,
    String timezoneSource
){
    public static PatrolExecutionDto fromEntity (
                                        PatrolExecution pe){
        if( pe == null ) {return null; }

        return new PatrolExecutionDto (
            pe.getId(),
            pe.getExternalId(),
            pe.getStartTime(),
            pe.getEndTime(),
            pe.getPatrolExecutionStatus(),
            pe.getUserId(),
            pe.getEmployeeId(),
            pe.getDescription(),
            pe.getPhotoPath(),
            pe.getVideoPath(),
            pe.getLatitude(),
            pe.getLongitude(),
            pe.getClientTimezone(),
            pe.getTimezoneSource()
        );
    }
}
