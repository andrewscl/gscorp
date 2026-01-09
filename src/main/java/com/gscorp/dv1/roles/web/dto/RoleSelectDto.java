package com.gscorp.dv1.roles.web.dto;

import com.gscorp.dv1.roles.infrastructure.RoleSelectProjection;

public record RoleSelectDto (
    Long id,
    String role
){
    public static RoleSelectDto fromProjection(RoleSelectProjection projection) {

        if(projection == null) return null;

        return new RoleSelectDto(
            projection.getId(),
            projection.getRole()
        );
    }
}
