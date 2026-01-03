package com.gscorp.dv1.users.web.dto;

import com.gscorp.dv1.users.infrastructure.UserTableProjection;

public record UserTableDto (

    Long id,
    String username,
    String mail,
    String phone,
    Boolean active,
    Long employeeId

){

    public static UserTableDto fromProjection (UserTableProjection p) {

        if (p == null) return null;

        return new UserTableDto(
            p.getId(),
            p.getUsername(),
            p.getMail(),
            p.getPhone(),
            p.getActive(),
            p.getEmployeeId()
        );
    }

}
