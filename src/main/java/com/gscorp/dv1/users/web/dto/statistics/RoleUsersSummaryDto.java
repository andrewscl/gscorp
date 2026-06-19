package com.gscorp.dv1.users.web.dto.statistics;

import com.gscorp.dv1.users.infrastructure.projections.statistics.RoleUsersSummaryProjection;

public record RoleUsersSummaryDto (
    String role,
    Long totalUsers
){
    public static RoleUsersSummaryDto fromProjection(RoleUsersSummaryProjection p){
        if(p == null) return null;

        return new RoleUsersSummaryDto(
            p.getRole(),
            p.getTotalUsers()
        );
    }
}
