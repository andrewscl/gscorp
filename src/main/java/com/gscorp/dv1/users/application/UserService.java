package com.gscorp.dv1.users.application;

import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;

import com.gscorp.dv1.enums.UserStatus;
import com.gscorp.dv1.users.infrastructure.User;
import com.gscorp.dv1.users.web.dto.CreateUserRequest;
import com.gscorp.dv1.users.web.dto.InviteUserRequest;
import com.gscorp.dv1.users.web.dto.UserTableDto;
import com.gscorp.dv1.users.web.dto.UserUpdateDto;
import com.gscorp.dv1.users.web.dto.UserViewDto;

public interface UserService {
    Long createUser(CreateUserRequest req);
    void deleteById(Long id);
    List<User> findAll();
    User findById(Long id);
    UserViewDto findWithRolesAndClientsById(Long id);

    List<User> findAllWithCompaniesAndClients();
    
    User createInvitedUser(InviteUserRequest request);
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
    Optional<Long> findEmployeeIdByUserId(Long userId);
    Page<UserTableDto> getAllUsersWithEmployee(int page, int size);

    Page<UserTableDto> searchUsersWithEmployee(
        String q, UserStatus status, int page, int size);

    Map<String, Long> getUsersStatistics ();

}
