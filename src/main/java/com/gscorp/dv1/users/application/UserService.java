package com.gscorp.dv1.users.application;

import java.util.List;

import com.gscorp.dv1.users.infrastructure.User;
import com.gscorp.dv1.users.web.dto.CreateUserRequest;
import com.gscorp.dv1.users.web.dto.InviteUserRequest;

public interface UserService {
    Long createUser(CreateUserRequest req);
    void deleteById(Long id);
    List<User> findAll();
    User findById(Long id);
    User findWithRolesAndClientsById(Long id);
    List<User> findAllWithRolesAndClients();
    User createInvitedUser(InviteUserRequest request);
    Boolean isInvitationTokenValid(String token);
    Boolean setPasswordFromInvitation(String token, String password);
    void save(User user);
}
