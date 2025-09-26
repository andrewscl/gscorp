package com.gscorp.dv1.roles.web;


import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gscorp.dv1.api.dto.RoleDto;
import com.gscorp.dv1.entities.Role;
import com.gscorp.dv1.roles.application.RoleService;

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
    public ResponseEntity <List<RoleDto>> getAllRoles() {
        return ResponseEntity.ok(roleService.getAllDtos());
    }

}
