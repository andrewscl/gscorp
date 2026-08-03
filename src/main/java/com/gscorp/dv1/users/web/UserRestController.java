package com.gscorp.dv1.users.web;

import java.net.URI;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.gscorp.dv1.admin.dashboard.web.dto.AdminDistributionMetricResponse;
import com.gscorp.dv1.config.security.SecurityUser;
import com.gscorp.dv1.users.application.UserService;
import com.gscorp.dv1.users.application.UserStatService;
import com.gscorp.dv1.users.infrastructure.User;
import com.gscorp.dv1.users.web.dto.CreateUserRequest;
import com.gscorp.dv1.users.web.dto.UserDto;
import com.gscorp.dv1.users.web.dto.UserUpdateDto;
import com.gscorp.dv1.users.web.dto.UserViewDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMINISTRATOR')")
public class UserRestController {

    private final UserService userService;
    private final UserStatService userStatService;

    @PostMapping("/create")
    public ResponseEntity<?> createUser(@RequestBody CreateUserRequest req) {
            Long id = userService.createUser(req);
            return ResponseEntity
                    .created(URI.create("/api/users/" + id)).build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
                userService.deleteById(id);
                return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getById(@PathVariable Long id) {
                User user = userService.findById(id);
                return ResponseEntity.ok(UserDto.fromEntity(user));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UserViewDto> patchUser(
                        @PathVariable("id") UUID userExternalId,
                        @RequestBody JsonNode body,
                        @AuthenticationPrincipal SecurityUser securityUser) {

        if (securityUser == null) {
            throw new AuthenticationCredentialsNotFoundException("Usuario no autenticado");
        }
        if (userExternalId == null) throw new IllegalArgumentException("userId requerido");
        if (body == null || body.isNull()) throw new IllegalArgumentException("body requerido");

        UserUpdateDto dto = UserUpdateDto.fromJson(body);

        return userService.updateUser(userExternalId, dto)
                .map(UserViewDto::from)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
        }

    @GetMapping("/admin-dashboard-metrics")
    public AdminDistributionMetricResponse getAdminDashboardMetrics(){
        AdminDistributionMetricResponse metrics =
            new AdminDistributionMetricResponse(
                userStatService.getUsersStatusSummary(),
                userStatService.getRoleUsersSummary()
            );
        return metrics;
    }

}
