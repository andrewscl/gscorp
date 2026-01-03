package com.gscorp.dv1.users.infrastructure;

public interface UserTableProjection {
    Long getId();
    String getUsername();
    String getMail();
    String getPhone();
    Boolean getActive();
    Long getEmployeeId();
    
}
