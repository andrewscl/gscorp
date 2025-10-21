package com.gscorp.dv1.nationalities.application;

import com.gscorp.dv1.nationalities.infrastructure.Nationality;

public interface NationalityService {

    Nationality findById(Long id);

}
