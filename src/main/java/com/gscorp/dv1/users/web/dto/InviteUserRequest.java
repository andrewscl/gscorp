package com.gscorp.dv1.users.web.dto;

import java.util.Set;

public record InviteUserRequest (
    String username,
    String mail,
    Long roleId,
    Set<Long> companyIds,
    Set<Long> clientIds,
    Long employeeId
){}
