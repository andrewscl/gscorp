package com.gscorp.dv1.operations.shiftpatterns.application;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gscorp.dv1.operations.shiftpatterns.infrastructure.ShiftPattern;
import com.gscorp.dv1.operations.shiftpatterns.infrastructure.ShiftPatternRepository;
import com.gscorp.dv1.operations.shiftpatterns.web.dto.ShiftPatternDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ShiftPatternServiceimpl implements ShiftPatternService {

    private final ShiftPatternRepository shiftPatternRepository;

    @Transactional(readOnly = true)
    public ShiftPattern findById(Long id) {
        return shiftPatternRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Shift Pattern not found with id: " + id));
    }


    @Transactional(readOnly = true)
    public Optional<ShiftPatternDto> findByExternalId(UUID externalId) {
        return shiftPatternRepository.findByExternalId(externalId)
                                .map(ShiftPatternDto::fromProjection);
    }

    @Transactional(readOnly = true)
    public Set<ShiftPattern> findAll() {
        return new HashSet<>(shiftPatternRepository.findAll());
    }

    @Transactional
    public ShiftPattern saveShiftPattern(ShiftPattern shiftPattern) {
        return shiftPatternRepository.save(shiftPattern);
    }

    @Transactional(readOnly = true)
    public List<ShiftPatternDto> getShiftPatternsList() {
        return shiftPatternRepository.findAll().stream()
                .map(ShiftPatternDto::fromEntity)
                .toList();
    }

}
