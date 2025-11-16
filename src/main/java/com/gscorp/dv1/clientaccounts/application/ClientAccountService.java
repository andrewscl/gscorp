package com.gscorp.dv1.clientaccounts.application;

import java.util.List;

import org.springframework.security.core.Authentication;

import com.gscorp.dv1.clientaccounts.web.dto.ClientAccountDto;

public interface ClientAccountService {
    
    /**
     * Devuelve ClientAccountDto para el userId indicado.
     */
    List<ClientAccountDto> findAccountDtosForUser(Long userId);

    /**
     * Conveniencia: resuelve el userId desde Authentication usando UserService internamente
     * y delega a {@link #findAccountDtosForUser(Long)}.
     */
    List<ClientAccountDto> findAccountDtosForPrincipal(Authentication authentication);

}
