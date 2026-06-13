package com.gscorp.dv1.professions.application;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.gscorp.dv1.professions.infrastructure.Profession;
import com.gscorp.dv1.professions.web.dto.ProfessionSelectDto;

public interface ProfessionService {

    Profession findById(Long id);

    Set<Profession> findAllById(Set<Long> ids);

    Set<Profession> findAll();

    Profession saveProfession(Profession profession);

    List<ProfessionSelectDto>
        findProfessionSelectDtosByEmployeeId(UUID employeeExternalId);

}
