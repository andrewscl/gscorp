package com.gscorp.dv1.roles.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.AllArgsConstructor;

@Controller
@RequestMapping("/private/admin/roles")
@AllArgsConstructor
public class RoleController {
    
    @GetMapping("/create")
    public String createRoleView() {
        return "private/admin/views/create-role-view";
    }
 
}
