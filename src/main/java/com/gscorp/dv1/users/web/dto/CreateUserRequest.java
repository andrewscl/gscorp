package com.gscorp.dv1.users.web.dto;

import java.util.Set;

public record CreateUserRequest (
    String username,
    String mail,
    String password,
    Set<Long> roleIds
){}
