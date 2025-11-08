package com.gscorp.dv1.shiftrequests.application;

import java.util.List;
import java.util.Optional;

import com.gscorp.dv1.shiftrequests.web.dto.CreateShiftRequestRequest;
import com.gscorp.dv1.shiftrequests.web.dto.ShiftRequestDto;

public interface ShiftRequestService {
    List<ShiftRequestDto> findAll();
    Optional<ShiftRequestDto> findById(Long id);
    ShiftRequestDto create(CreateShiftRequestRequest shiftRequest);
    Optional<ShiftRequestDto> update(Long id, CreateShiftRequestRequest createShiftRequestDto);
}
