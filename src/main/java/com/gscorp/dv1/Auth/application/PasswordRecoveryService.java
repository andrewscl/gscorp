package com.gscorp.dv1.auth.application;

import java.util.Optional;
import com.gscorp.dv1.auth.web.dto.PasswordRecoveryDto;

public interface PasswordRecoveryService {

    Optional<PasswordRecoveryDto> prepareRecovery(String usernameOrEmail);

    void executeRecovery(PasswordRecoveryDto dto);
    
}
