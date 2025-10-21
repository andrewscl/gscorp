package com.gscorp.dv1.nationalities.application;

import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gscorp.dv1.nationalities.infrastructure.Nationality;
import com.gscorp.dv1.nationalities.infrastructure.NationalityRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NationalityServiceImpl implements NationalityService {

    private final NationalityRepository nationalityRepository;

    @Override
    @Transactional(readOnly = true)
    public Nationality findById(Long id) {
        return nationalityRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Nacionalidad no encontrada"));
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Nationality> findAll() {
        return Set.copyOf(nationalityRepository.findAll());
    }

}
