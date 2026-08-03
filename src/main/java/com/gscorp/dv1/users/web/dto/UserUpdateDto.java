package com.gscorp.dv1.users.web.dto;

import java.util.Set;

import com.gscorp.dv1.enums.UserStatus;

public record UserUpdateDto(
    String username,
    String mail,
    Boolean active,
    UserStatus status,
    Long roleId,
    Set<Long> companyIds,
    Set<Long> clientIds,
    Long employeeId,
    String timeZone
) {}