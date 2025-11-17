package com.gscorp.dv1.clientaccounts.application;

import java.util.List;

import org.springframework.security.core.Authentication;

import com.gscorp.dv1.clientaccounts.web.dto.ClientAccountDto;
import com.gscorp.dv1.clientaccounts.web.dto.CreateClientAccountRequest;

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

        // Crea una cuenta asociada a clientId. Valida que el client pertenezca al userId.
    ClientAccountDto createClientAccount(CreateClientAccountRequest req, Long userId);

    // Conveniencia: resuelve userId desde Authentication y delega
    ClientAccountDto createClientAccountForPrincipal(CreateClientAccountRequest req, Authentication authentication);

    // Opcional: método que devuelve la entidad o lanza si no existe / no pertenece (útil para edit/show)
    // ClientAccount getAccountIfOwned(Long accountId, Long userId);

    ClientAccountDto getAccountDtoIfOwned(Long accountId, Long userId);

    /**
     * Obtiene las cuentas (ClientAccountDto) asociadas al client del site dado,
     * considerando el userId para aplicar reglas de visibilidad/membership.
     *
     * Comportamiento:
     * - Si siteId es null o no existe -> devuelve lista vacía.
     * - Si userId es null -> lanza AuthenticationCredentialsNotFoundException.
     * - Si el usuario no pertenece al client -> lanza AccessDeniedException.
     *
     * @param siteId id del Site
     * @param userId id del usuario autenticado
     * @return lista de ClientAccountDto
     */
    List<ClientAccountDto> getClientAccountsForSite(Long siteId, Long userId);

}
