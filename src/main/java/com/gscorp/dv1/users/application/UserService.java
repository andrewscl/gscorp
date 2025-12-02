package com.gscorp.dv1.users.application;

import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import org.springframework.security.core.Authentication;

import com.gscorp.dv1.users.infrastructure.User;
import com.gscorp.dv1.users.web.dto.CreateUserRequest;
import com.gscorp.dv1.users.web.dto.InviteUserRequest;
import com.gscorp.dv1.users.web.dto.InviteUserRequestWhatsApp;
import com.gscorp.dv1.users.web.dto.UserUpdateDto;

public interface UserService {
    Long createUser(CreateUserRequest req);
    void deleteById(Long id);
    List<User> findAll();
    User findById(Long id);

    

    User findWithRolesAndClientsById(Long id);
    List<User> findAllWithRolesAndClients();

    User createInvitedUser(InviteUserRequest request);
    User createInvitedUserWhatsApp(InviteUserRequestWhatsApp request);

    Boolean isInvitationTokenValid(String token);
    Boolean setPasswordFromInvitation(String token, String password);
    void save(User user);
    Boolean isAdmin(User user);
    Optional<User> findByUsername(String username);
    Long getUserIdFromAuthentication(Authentication authentication);
    boolean isAdmin(Authentication authentication);




    List<Long> getClientIdsForUser(Long userId);

    /**
     * Devuelve la zona de usuario (ID de ZoneId, p.ej. "Europe/Madrid") para el userId dado.
     *cd .. Devuelve Optional.empty() si no existe user o no está definida/válida.
     */
    Optional<ZoneId> getUserZone(Long userId);

    Optional<User> updateUser(Long userId, UserUpdateDto dto);


}
