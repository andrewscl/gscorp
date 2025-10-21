package com.gscorp.dv1.shiftpatterns.application;

import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.gscorp.dv1.shiftpatterns.infrastructure.ShiftPattern;
import com.gscorp.dv1.shiftpatterns.infrastructure.ShiftPatternRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ShiftPatternServiceimpl implements ShiftPatternService {

    private final ShiftPatternRepository shiftPatternRepository;

    @Override
    public ShiftPattern findById(Long id) {
        return shiftPatternRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Shift Pattern not found with id: " + id));
    }

    @Override
    public Set<ShiftPattern> findAll() {
        return new HashSet<>(shiftPatternRepository.findAll());
    }
    
}
