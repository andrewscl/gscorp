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
import com.gscorp.dv1.operations.shiftpatterns.web.dto.UpdateShiftPatternRequest;

import jakarta.persistence.EntityNotFoundException;
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
    public ShiftPatternDto findDtoByExternalId(UUID externalId) {

        return shiftPatternRepository.findProjectionByExternalId(externalId)
                    .map(ShiftPatternDto::fromProjection)
                    .orElseThrow(
                        ()-> new EntityNotFoundException("Sistema de jornada no enconrado."));
    }

    @Transactional(readOnly = true)
    public ShiftPattern findByExternalId(UUID externalId) 
    {
        return shiftPatternRepository.findByExternalId(externalId).get();
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

    @Transactional
    public ShiftPatternDto updateShiftPattern(
                            UUID shiftPatternExternalId,
                            UpdateShiftPatternRequest request) {
        if (shiftPatternExternalId == null)
            throw new IllegalArgumentException("shiftPatternExternalId es requerido");
        if (request == null)
            throw new IllegalArgumentException("el shiftPatternRequest es requerido");
        Optional<ShiftPattern> optShiftPattern =
            shiftPatternRepository.findByExternalId(shiftPatternExternalId);
        ShiftPattern shiftPattern = optShiftPattern.get();

        if (request.name() == null || request.code() == null || 
            request.workDays() == null || request.restDays() == null) {
                throw new IllegalArgumentException("falta información para actualizar.");
            }

        if (request.name() != null) shiftPattern.setName(request.name().trim());
        if (request.code() != null) shiftPattern.setCode(request.code().trim());
        if (request.description() != null) shiftPattern.setDescription(request.description().trim());
        if (request.workDays() != null) shiftPattern.setWorkDays(request.workDays());
        if (request.restDays() != null) shiftPattern.setRestDays(request.restDays());

        ShiftPattern saved = shiftPatternRepository.save(shiftPattern);

        return ShiftPatternDto.fromEntity(saved);
    }

}
