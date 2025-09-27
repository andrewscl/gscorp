package com.gscorp.dv1.roles.application;

import java.util.List;

import com.gscorp.dv1.api.dto.RoleDto;
import com.gscorp.dv1.entities.Role;

public interface RoleService {
    void saveRole (Role role);
    Role findById (Long id);
    List<RoleDto> getAllRoles();
}
