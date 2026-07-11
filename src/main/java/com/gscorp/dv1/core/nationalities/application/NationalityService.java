package com.gscorp.dv1.core.nationalities.application;

import java.util.Set;

import com.gscorp.dv1.core.nationalities.infrastructure.Nationality;

public interface NationalityService {

    Nationality findById(Long id);
    Set<Nationality> findAll();
    Nationality saveNationality(Nationality nationality);

}
