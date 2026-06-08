package com.gscorp.dv1.roles.web.dto;

import com.gscorp.dv1.enums.AccountType;
import com.gscorp.dv1.roles.infrastructure.Role;

public record RoleDto (
    Long id,
    String externalId,
    String name,
    AccountType accountType
){
    public static RoleDto fromEntity (Role role) {
        if(role==null) return null;

        return new RoleDto(
            role.getId(),
            role.getExternalId().toString(),
            role.getRole(),
            role.getAccountType()
        );
    }
}
