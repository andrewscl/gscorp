package com.gscorp.dv1.clientaccounts.application;

import java.util.Collections;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gscorp.dv1.clientaccounts.infrastructure.ClientAccountRepository;
import com.gscorp.dv1.clientaccounts.web.dto.ClientAccountDto;
import com.gscorp.dv1.users.application.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClientAccountServiceImpl implements ClientAccountService {

    private final UserService userService;
    private final ClientAccountRepository clientAccountRepository;
    
    /**
     * Devuelve una lista de ClientAccountDto asociadas a todos los Clients del usuario.
     *
     * @param userId id del usuario
     * @return lista (posiblemente vacía) de ClientAccountDto
     */
    @Override
    @Transactional(readOnly = true)
    public List<ClientAccountDto> findAccountDtosForUser(Long userId) {
        if (userId == null) return Collections.emptyList();

        List<Long> clientIds = userService.getClientIdsForUser(userId);
        if (clientIds == null || clientIds.isEmpty()) {
            return Collections.emptyList();
        }

        // El repositorio ya tiene la consulta que devuelve ClientAccountDto vía JPQL constructor expression
        return clientAccountRepository.findDtoByClientIds(clientIds);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClientAccountDto> findAccountDtosForPrincipal(Authentication authentication) {
        Long userId = userService.getUserIdFromAuthentication(authentication);
        return findAccountDtosForUser(userId);
    }

}
