package com.gscorp.dv1.users.web.dto;

import java.util.Set;

public record CreateUserRequest (
    String username,
    String mail,
    String password,
    Long roleId,
    Set<Long> companyIds,
    Set<Long> clientIds,
    Long employeeId
){}
