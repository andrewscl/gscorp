package com.gscorp.dv1.api.dto;

import java.util.Set;

public record CreateUserRequest (
    String username,
    String password,
    Set<Long> roleIds
){}
