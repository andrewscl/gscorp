package com.gscorp.dv1.users.web.dto;

import java.util.Set;
import java.util.stream.Collectors;

import com.gscorp.dv1.roles.infrastructure.Role;
import com.gscorp.dv1.users.infrastructure.User;

public record UserDto (Long id, String username, String mail, Set<String> roles) {

    public static UserDto fromEntity(User u) {
        return new UserDto(
            u.getId(),
            u.getUsername(),
            u.getMail(),
            u.getRoles().stream().map(Role::getRole).collect(Collectors.toSet())
        );
    }
}
