package com.gscorp.dv1.users.infrastructure;

import com.gscorp.dv1.enums.UserStatus;

public interface UserTableProjection {
    Long getId();
    String getUsername();
    String getMail();
    String getPhone();
    Boolean getActive();
    UserStatus getStatus();
    Long getEmployeeId();
    
}
