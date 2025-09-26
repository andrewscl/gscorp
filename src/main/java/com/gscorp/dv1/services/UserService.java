package com.gscorp.dv1.services;

import com.gscorp.dv1.api.dto.CreateUserRequest;

public interface UserService {
    Long createUser(CreateUserRequest req);
}
