package com.gscorp.dv1.users.web.dto;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.gscorp.dv1.enums.UserStatus;
import com.gscorp.dv1.users.infrastructure.User;

/**
 * DTO de salida seguro para devolver información de usuario en responses.
 */
public record UserViewDto(
        Long id,
        UUID externalId,
        String username,
        String mail,
        UserStatus status,
        Boolean active,
        Long roleId,
        String roleName,
        Set<Long> companyIds,
        Set<String> companyNames,
        Set<Long> clientIds,
        Set<String> clientNames,
        Long employeeId,
        String employeeName,
        String timeZone
) {
    public static UserViewDto from(User u) {
        Long roleId = u.getRole() == null ? null : u.getRole().getId();
        String roleName = u.getRole() == null ? null : u.getRole().getRole();

        Set<Long> companyIds = u.getCompanies() == null ? Collections.emptySet()
                : u.getCompanies().stream()
                    .filter(Objects::nonNull)
                    .map(company -> company.getId())
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

        Set<String> companyNames = u.getCompanies() == null ? Collections.emptySet()
                : u.getCompanies().stream()
                    .filter(Objects::nonNull)
                    .map(company -> company.getName())
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
        
        Set<String> clientNames = u.getClients() == null ? Collections.emptySet()
                : u.getClients().stream()
                    .filter(Objects::nonNull)
                    .map(client -> client.getName())
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

        Set<Long> clientIds = u.getClients() == null ? Collections.emptySet()
                : u.getClients().stream()
                    .filter(Objects::nonNull)
                    .map(client -> client.getId())
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
                u.getExternalId(),
                u.getUsername(),
                u.getMail(),
                u.getStatus(),
                u.getActive(),
                roleId,
                roleName,
                companyIds,
                companyNames,
                clientIds,
                clientNames,
                empId,
                empName,
                u.getTimeZone()
        );
    }
}