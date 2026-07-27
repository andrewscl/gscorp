package com.gscorp.dv1.operations.shiftassignments.application;

import java.util.UUID;

import org.springframework.data.domain.Page;

import com.gscorp.dv1.enums.ShiftAssignmentStatus;
import com.gscorp.dv1.operations.shiftassignments.web.dto.ShiftAssignmentDto;

public interface ShiftAssignmentService {

    Page<ShiftAssignmentDto> getShiftAssignmentList(
        UUID userExternalId,
        ShiftAssignmentStatus status,
        int page,
        int size
    );

}
