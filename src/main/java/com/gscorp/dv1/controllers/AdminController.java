package com.gscorp.dv1.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gscorp.dv1.repositories.ContactRepository;
import com.gscorp.dv1.repositories.LicitationRepository;
import com.gscorp.dv1.repositories.RoleRepository;
import com.gscorp.dv1.services.MpSyncService;
import com.gscorp.dv1.users.infrastructure.UserRepository;

import lombok.AllArgsConstructor;

@Controller
@RequestMapping("/private/admin")
@AllArgsConstructor
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private LicitationRepository licitationRepository;

    @Autowired
    private final MpSyncService sync;

    @GetMapping("/dashboard")
    public String getPrivateDashboardView(Model model) {
        return "private/admin/views/admin-dashboard-view";
    }

    @GetMapping("/users")
    public String getUsersTableView(Model model) {
        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("roles", roleRepository.findAll());
        return "private/admin/views/users-table-view";
    }

    @GetMapping("/roles")
    public String getRolesTableView(Model model) {
        model.addAttribute("roles", roleRepository.findAll());
        return "private/admin/views/roles-table-view";
    }

    @GetMapping("/roles/create")
    public String createRoleView() {
        return "private/admin/views/create-role-view";
    }

    @GetMapping("/contacts")
    public String getContactsTableView(Model model) {
        model.addAttribute("contacts", contactRepository.findAll());
        return "private/admin/views/contacts-table-view";
    }

    @GetMapping("/mp-licitations")
    public String getLicitationsTableView(Model model) {
        model.addAttribute("licitations", licitationRepository.findAll());
        return "private/admin/views/licitations-table-view";
    }

    @GetMapping("/mp-licitations/sync")
    public String syncLicitations(Model model) {
        sync.syncTodayLicitations();
        return "private/admin/views/licitations-table-view";
    }

}
