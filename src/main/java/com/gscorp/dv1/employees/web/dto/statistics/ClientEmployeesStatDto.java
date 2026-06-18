package com.gscorp.dv1.employees.web.dto.statistics;

import com.gscorp.dv1.employees.infrastructure.Projections.statistics.ClientEmployeesStatProjection;

public record ClientEmployeesStatDto (
    String clientName,
    long hiredCount,
    long activeCount,
    long noticeGivenCount,
    long inactiveCount,
    long settledCount,
    long invitedUsersCount,
    long activeUsersCount,
    long inactiveUsersCount,
    long expiredUsersCount,
    long suspendedUsersCount
){
    public static ClientEmployeesStatDto fromProjection(ClientEmployeesStatProjection p) {
        if (p == null) return null;
        return new ClientEmployeesStatDto (
            p.getClientName(),
            p.getHiredCount(),
            p.getActiveCount(),
            p.getNoticeGivenCount(),
            p.getInactiveCount(),
            p.getSettledCount(),
            p.getInvitedUsersCount(),
            p.getActiveUsersCount(),
            p.getInactiveUsersCount(),
            p.getExpiredUsersCount(),
            p.getSuspendedUsersCount()
        );
    }
}
