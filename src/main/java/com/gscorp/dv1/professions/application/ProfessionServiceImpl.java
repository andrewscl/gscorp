package com.gscorp.dv1.professions.application;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gscorp.dv1.professions.infrastructure.Profession;
import com.gscorp.dv1.professions.infrastructure.ProfessionRepository;
import com.gscorp.dv1.professions.web.dto.ProfessionSelectDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProfessionServiceImpl implements ProfessionService {

    private final ProfessionRepository professionRepository;

    @Override
    @Transactional(readOnly = true)
    public Profession findById(Long id) {
        return professionRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Profession> findAllById(Set<Long> ids) {
        return new HashSet<>(professionRepository.findAllById(ids));
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Profession> findAll() {
        return new HashSet<>(professionRepository.findAll());
    }

    @Override
    @Transactional
    public Profession saveProfession(Profession profession) {
        return professionRepository.save(profession);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProfessionSelectDto>    
            findProfessionSelectDtosByEmployeeId(UUID employeeExternalId) {
        return professionRepository
                .findProfessionSelectDtosByEmployeeExternalId(employeeExternalId)
                .stream()
                .map(ProfessionSelectDto::fromProjection)
                .toList();
    }




}
