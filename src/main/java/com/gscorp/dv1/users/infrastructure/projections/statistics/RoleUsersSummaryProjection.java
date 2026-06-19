package com.gscorp.dv1.users.infrastructure.projections.statistics;

public interface RoleUsersSummaryProjection {
    String getRole();
    Long getTotalUsers();
}
