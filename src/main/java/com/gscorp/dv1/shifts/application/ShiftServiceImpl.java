package com.gscorp.dv1.shifts.application;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.gscorp.dv1.shifts.infrastructure.Shift;
import com.gscorp.dv1.shifts.infrastructure.ShitfRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ShiftServiceImpl implements ShiftService {

    private final ShitfRepository shiftRepository;

    @Override
    public List<Shift> getShifts(Long siteId, OffsetDateTime from, OffsetDateTime to) {
        return shiftRepository.findBySiteIdAndStartTsBetween(siteId, from, to);
    }
    
}
