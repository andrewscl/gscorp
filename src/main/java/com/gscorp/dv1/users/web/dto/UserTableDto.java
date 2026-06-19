package com.gscorp.dv1.users.web.dto;

import java.util.UUID;

import com.gscorp.dv1.enums.UserStatus;
import com.gscorp.dv1.users.infrastructure.User;
import com.gscorp.dv1.users.infrastructure.projections.UserTableProjection;

public record UserTableDto (

    Long id,
    UUID externalId,
    String username,
    String mail,
    String phone,
    Boolean active,
    UserStatus status,
    Long employeeId,
    String roleName

){

    public static UserTableDto fromProjection (UserTableProjection p) {
        if (p == null) return null;
        return new UserTableDto(
            p.getId(),
            p.getExternalId(),
            p.getUsername(),
            p.getMail(),
            p.getPhone(),
            p.getActive(),
            p.getStatus(),
            p.getEmployeeId(),
            p.getRoleName()
        );
    }

    public static UserTableDto fromEntity (User user){
        if (user == null) return null;
        return new UserTableDto(
            user.getId(),
            user.getExternalId(),
            user.getUsername(),
            user.getMail(),
            user.getPhone(),
            user.getActive(),
            user.getStatus(),
            user.getEmployee() != null ? user.getEmployee().getId() : null,
            user.getRole() != null ? user.getRole().getRole() : null
        );
    }

}
