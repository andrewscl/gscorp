package com.gscorp.dv1.roles.application;

import java.util.List;

import org.springframework.stereotype.Service;

import com.gscorp.dv1.roles.infrastructure.Role;
import com.gscorp.dv1.roles.web.dto.RoleDto;
import com.gscorp.dv1.roles.web.dto.RoleSelectDto;
import com.gscorp.dv1.roles.web.dto.UpdateRoleRequest;

@Service
public interface RoleService {
    void saveRole (Role role);
    Role findById (Long id);
    List<RoleDto> getAllRoles();
    Role findWithUsersById(Long id);

    List<RoleSelectDto> getAllRolesSelectDto();

    RoleDto patchRole (String externalId, UpdateRoleRequest request); 

    RoleDto findByExternalId(String externalId);


}
