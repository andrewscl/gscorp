package com.gscorp.dv1.users.web.dto;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.gscorp.dv1.clients.infrastructure.Client;
import com.gscorp.dv1.enums.UserStatus;
import com.gscorp.dv1.roles.infrastructure.Role;
import com.gscorp.dv1.users.infrastructure.User;

/**
 * DTO de salida seguro para devolver información de usuario en responses.
 */
public record UserViewDto(
        Long id,
        String username,
        String mail,
        UserStatus status,
        Boolean active,
        Set<Long> roleIds,
        Set<String> roleNames,
        Set<Long> clientIds,
        Set<String> clientNames,
        Long employeeId,
        String employeeName,
        String timeZone
) {
    public static UserViewDto from(User u) {
        Set<Long> roleIds = u.getRoles() == null ? Collections.emptySet()
                : u.getRoles().stream()
                    .filter(Objects::nonNull)
                    .map(Role::getId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

        Set<String> roleNames = u.getRoles() == null ? Collections.emptySet()
                : u.getRoles().stream()
                    .filter(Objects::nonNull)
                    .map(r -> {
                        // si en el futuro añades getName(), cámbialo aquí; por ahora usamos role (String)
                        String name = r.getRole();
                        return name == null ? null : name;
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

        Set<Long> clientIds = u.getClients() == null ? Collections.emptySet()
                : u.getClients().stream()
                    .filter(Objects::nonNull)
                    .map(Client::getId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

        Set<String> clientNames = u.getClients() == null ? Collections.emptySet()
                : u.getClients().stream()
                    .filter(Objects::nonNull)
                    .map(Client::getName)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

        Long empId = u.getEmployee() == null ? null : u.getEmployee().getId();

        String empName = u.getEmployee() == null ? "ninguno" :
            String.join(" ",
                u.getEmployee().getName() != null ? u.getEmployee().getName() : "",
                u.getEmployee().getFatherSurname() != null ? u.getEmployee().getFatherSurname() : "",
                u.getEmployee().getMotherSurname() != null ? u.getEmployee().getMotherSurname() : ""
            ).trim();

        return new UserViewDto(
                u.getId(),
                u.getUsername(),
                u.getMail(),
                u.getStatus(),
                u.getActive(),
                roleIds,
                roleNames,
                clientIds,
                clientNames,
                empId,
                empName,
                u.getTimeZone()
        );
    }
}