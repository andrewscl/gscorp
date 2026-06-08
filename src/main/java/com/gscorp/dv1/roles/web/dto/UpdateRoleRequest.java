package com.gscorp.dv1.roles.web.dto;

import com.gscorp.dv1.enums.AccountType;
import com.gscorp.dv1.roles.infrastructure.Role;

public record UpdateRoleRequest (
    Long id,
    Role role,
    AccountType accountType
){}
