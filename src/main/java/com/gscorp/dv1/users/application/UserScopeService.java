package com.gscorp.dv1.users.application;

import java.util.List;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.gscorp.dv1.admin.projects.application.ProjectService;
import com.gscorp.dv1.admin.projects.web.dto.ProjectDto;
import com.gscorp.dv1.admin.projects.web.dto.ProjectSelectDto;
import com.gscorp.dv1.config.security.SecurityUser;
import com.gscorp.dv1.users.application.dto.ProjectScope;
import com.gscorp.dv1.users.infrastructure.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserScopeService {
    
    private final ProjectService projectService;

    public ProjectScope getProjectScope() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof SecurityUser securityUser)){
            return ProjectScope.restricted(List.of());
        }

        User user = securityUser.getUser();
        UUID userExternalId = user.getExternalId();

        boolean isAdmin = has(securityUser, "ROLE_ADMINISTRATOR");
        if (isAdmin) return ProjectScope.unrestricted();

        boolean isUserLevelScoped = has(securityUser, "ROLE_CLIENT") || has(securityUser, "ROLE_ANALYST");
        if (isUserLevelScoped) {
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

    private boolean has(SecurityUser securityUser, String role) {
        return securityUser != null && securityUser.getAuthorities().stream()
                .anyMatch(a-> a.getAuthority().equals(role));
    }
}