package com.gscorp.dv1.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gscorp.dv1.repositories.ContactRepository;
import com.gscorp.dv1.repositories.RoleRepository;
import com.gscorp.dv1.repositories.UserRepository;

@Controller
@RequestMapping("/private")
public class PrivateController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private RoleRepository roleRepository;

    @GetMapping("/dashboard")
    public String getPrivateDashboardView(Model model) {
        return "private/views/private-dashboard-view";
    }

    @GetMapping("/users")
    public String getUsersTableView(Model model) {
        model.addAttribute("users", userRepository.findAll());
        return "private/views/users-table-view";
    }

    @GetMapping("/roles")
    public String getRolesTableView(Model model) {
        model.addAttribute("roles", roleRepository.findAll());
        return "private/views/roles-table-view";
    }

    @GetMapping("/roles/create")
    public String createRoleView() {
        return "private/views/create-role-view";
    }

    @GetMapping("/contacts")
    public String getContactsTableView(Model model) {
        model.addAttribute("contacts", contactRepository.findAll());
        return "private/views/contacts-table-view";
    }

}
