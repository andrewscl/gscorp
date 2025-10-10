package com.gscorp.dv1.users.application;

import java.util.List;

import com.gscorp.dv1.users.infrastructure.User;
import com.gscorp.dv1.users.web.dto.CreateUserRequest;

public interface UserService {
    Long createUser(CreateUserRequest req);
    void deleteById(Long id);
    List<User> findAll();
    User findById(Long id);
    User findWithClientsById(Long id);
}
