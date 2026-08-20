package com.gscorp.dv1.operations.shiftassignments.application;

import java.util.UUID;

import org.springframework.data.domain.Page;

import com.gscorp.dv1.enums.ShiftAssignmentStatus;
import com.gscorp.dv1.operations.shiftassignments.web.dto.CreateShiftAssignmentRequest;
import com.gscorp.dv1.operations.shiftassignments.web.dto.ShiftAssignmentDto;

public interface ShiftAssignmentService {

    Page<ShiftAssignmentDto> getShiftAssignmentsList(
        UUID userExternalId,
        UUID siteExternalId,
        ShiftAssignmentStatus status,
        int page,
        int size,
        String requetedZone
    );

    ShiftAssignmentDto createShiftAssignment (
                            UUID userExternalId,
                            String requestedZone,
                            CreateShiftAssignmentRequest request);

    ShiftAssignmentDto getByExternalId(
                            UUID userExternalId,
                            UUID shiftAssignmentExternalId,
                            String requestedZone
                        );

}
