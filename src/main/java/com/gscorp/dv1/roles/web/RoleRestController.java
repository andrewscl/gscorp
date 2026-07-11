package com.gscorp.dv1.roles.web;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gscorp.dv1.roles.application.RoleService;
import com.gscorp.dv1.roles.infrastructure.Role;
import com.gscorp.dv1.roles.web.dto.RoleDto;
import com.gscorp.dv1.roles.web.dto.UpdateRoleRequest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleRestController {

    private final RoleService roleService;

    @PostMapping("/create")
    public ResponseEntity <?> createRole(@RequestBody Role role) {
        roleService.saveRole(role);
        return ResponseEntity.ok("Rol creado exitosamente");
    }

    @GetMapping("/all")
    public ResponseEntity<List<RoleDto>> getAllRoles() {
        List<RoleDto> roles = roleService.getAllRoles();
        return ResponseEntity.ok(roles);
    }


    @PatchMapping("/{externalId}")
    public ResponseEntity<RoleDto> patchRole(
                @PathVariable("externalId") String externalIdStr,
                @Valid @RequestBody UpdateRoleRequest req) {

        RoleDto roleDto = roleService.patchRole(
                                    externalIdStr,
                                    req);

        return ResponseEntity.ok(roleDto);
    }

}
