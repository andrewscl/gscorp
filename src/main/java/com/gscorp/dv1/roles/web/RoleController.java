package com.gscorp.dv1.roles.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gscorp.dv1.roles.application.RoleService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/private/admin/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;
    
    @GetMapping("/create")
    public String createRoleView() {
        return "private/admin/views/create-role-view";
    }

    @GetMapping("/show/{id}")
    public String showRole(@PathVariable Long id, Model model){
        var role = roleService.findWithUsersById(id);
        model.addAttribute("role", role);
        return "private/admin/roles/views/view-role-view";
    }

    @GetMapping("/edit/{id}")
    public String editRole(@PathVariable Long id, Model model){
        var role = roleService.findWithUsersById(id);
        model.addAttribute("role", role);
        return "private/admin/roles/views/edit-role-view";
    }

}
