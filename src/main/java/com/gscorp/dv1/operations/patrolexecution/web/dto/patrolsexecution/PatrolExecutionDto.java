package com.gscorp.dv1.operations.patrolexecution.web.dto.patrolsexecution;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.gscorp.dv1.operations.patrol.web.checkpoints.dto.PatrolCheckpointDto;
import com.gscorp.dv1.operations.patrolexecution.infrastructure.patrolsexecution.PatrolExecution;

public record PatrolExecutionDto (
    Long id,
    UUID externalId,
    Instant startTime,
    Instant endTime,
    String status,
    Long userId,
    Long employeeId,
    String description,
    String photoPath,
    String videoPath,
    BigDecimal latitude,
    BigDecimal longitude,
    String clientTimezone,
    String timezoneSource,
    String patrolName,
    String siteName,
    List<PatrolCheckpointDto> checkpoints
){
    public static PatrolExecutionDto fromEntity (
                                    PatrolExecution pe,
                                    String patrolName,
                                    String siteName,
                                    List<PatrolCheckpointDto> checkpoints){

        if( pe == null ) {return null; }

        return new PatrolExecutionDto (
            pe.getId(),
            pe.getExternalId(),
            pe.getStartTime(),
            pe.getEndTime(),
            pe.getStatus().getDisplayName(),
            pe.getUserId(),
            pe.getEmployeeId(),
            pe.getDescription(),
            pe.getPhotoPath(),
            pe.getVideoPath(),
            pe.getLatitude(),
            pe.getLongitude(),
            pe.getClientTimezone(),
            pe.getTimezoneSource(),
            patrolName,
            siteName,
            checkpoints
        );
    }
}
