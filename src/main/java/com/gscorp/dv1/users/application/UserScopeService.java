package com.gscorp.dv1.users.application;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.gscorp.dv1.admin.projects.application.ProjectService;
import com.gscorp.dv1.admin.projects.web.dto.ProjectDto;
import com.gscorp.dv1.admin.projects.web.dto.ProjectSelectDto;
import com.gscorp.dv1.config.security.SecurityUser;
import com.gscorp.dv1.roles.infrastructure.Role;
import com.gscorp.dv1.users.application.dto.ProjectScope;
import com.gscorp.dv1.users.infrastructure.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserScopeService {
    
    private final ProjectService projectService;

    public ProjectScope getProjectScope(
                                SecurityUser securityUser) {
        if (securityUser == null) return ProjectScope.restricted(List.of());

        User user = securityUser.getUser();
        UUID userExternalId = user.getExternalId();
        Role role = user.getRole();
        String roleName = (role != null) ? role.getRole() : "" ;

        boolean isAdmin = "ROLE_ADMINISTRATOR".equalsIgnoreCase(roleName)
                            || "ADMINISTRATOR".equalsIgnoreCase(roleName);
        if (isAdmin) return ProjectScope.unrestricted();

        boolean isClient = "ROLE_CLIENT".equalsIgnoreCase(roleName)
                            || "CLIENT".equalsIgnoreCase(roleName);
        if (isClient) {
            List<ProjectDto> projects =projectService.findByUserExternalId(userExternalId);
            List<Long> ids = (projects != null)
                            ? projects.stream().map(dto -> dto.id()).toList()
                            : List.of();
            return ProjectScope.restricted(ids);
        }
        if (user.getEmployee() != null && user.getEmployee().getExternalId() != null){
            UUID employeeExternalId = user.getEmployee().getExternalId();
            List<ProjectSelectDto> projects = 
                        projectService.findProjectSelectDtosByEmployeeExternalId(employeeExternalId);
            List<Long> ids = (projects != null)
                            ? projects.stream().map(dto -> dto.id()).toList()
                            : List.of();
            return ProjectScope.restricted(ids);
        }
        return ProjectScope.restricted(List.of());
    }
}