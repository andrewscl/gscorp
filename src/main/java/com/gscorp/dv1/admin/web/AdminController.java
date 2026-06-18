package com.gscorp.dv1.admin.web;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gscorp.dv1.repositories.ContactRepository;
import com.gscorp.dv1.roles.infrastructure.RoleRepository;
import com.gscorp.dv1.users.application.UserService;

import lombok.AllArgsConstructor;

@Controller
@RequestMapping("/private/admin")
@AllArgsConstructor
public class AdminController {

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserService userService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public String getPrivateDashboardView(
            Model model) {
        Map<String, Long> usersStats = userService.getUsersStatistics();
        model.addAllAttributes(usersStats);
        return "private/admin/dashboards/templates/admin-dashboard-view";
    }

    @GetMapping("/roles")
    public String getRolesTableView(Model model) {
        model.addAttribute("roles", roleRepository.findAll());
        return "private/roles/views/roles-table-view";
    }

    @GetMapping("/contacts")
    public String getContactsTableView(Model model) {
        model.addAttribute("contacts", contactRepository.findAll());
        return "private/admin/views/contacts-table-view";
    }

}
