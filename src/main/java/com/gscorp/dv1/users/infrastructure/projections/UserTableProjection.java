package com.gscorp.dv1.users.infrastructure.projections;

import java.util.UUID;

import com.gscorp.dv1.enums.UserStatus;

public interface UserTableProjection {
    Long getId();
    UUID getExternalId();
    String getUsername();
    String getMail();
    String getPhone();
    Boolean getActive();
    UserStatus getStatus();
    Long getEmployeeId();
    String getRoleName();
    
}
