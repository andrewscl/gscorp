package com.gscorp.dv1.professions.application;

import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.gscorp.dv1.professions.infrastructure.Profession;
import com.gscorp.dv1.professions.infrastructure.ProfessionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProfessionServiceImpl implements ProfessionService {

    private final ProfessionRepository professionRepository;

    @Override
    public Profession findById(Long id) {
        return professionRepository.findById(id).orElse(null);
    }

    @Override
    public Set<Profession> findAllById(Set<Long> ids) {
        return new HashSet<>(professionRepository.findAllById(ids));
    }

    @Override
    public Set<Profession> findAll() {
        return new HashSet<>(professionRepository.findAll());
    }
}
