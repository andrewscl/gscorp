package com.gscorp.dv1.roles.application;

import com.gscorp.dv1.entities.Role;

public interface RoleService {
    void saveRole (Role role);
    Role findById (Long id);
}
