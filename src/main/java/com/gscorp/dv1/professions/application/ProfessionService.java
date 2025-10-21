package com.gscorp.dv1.professions.application;

import java.util.Set;

import com.gscorp.dv1.professions.infrastructure.Profession;

public interface ProfessionService {

    Profession findById(Long id);

    Set<Profession> findAllById(Set<Long> ids);
}
