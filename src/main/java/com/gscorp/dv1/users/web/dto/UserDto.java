package com.gscorp.dv1.users.web.dto;

import java.util.UUID;

import com.gscorp.dv1.roles.infrastructure.Role;
import com.gscorp.dv1.users.infrastructure.User;

public record UserDto (
    Long id,
    UUID externalId,
    String username, 
    String mail, 
    Role role) {

    public static UserDto fromEntity(User u) {
        return new UserDto(
            u.getId(),
            u.getExternalId(),
            u.getUsername(),
            u.getMail(),
            u.getRole()
        );
    }
}
