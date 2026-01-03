package com.gscorp.dv1.users.web;

import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.gscorp.dv1.clients.application.ClientService;
import com.gscorp.dv1.employees.application.EmployeeService;
import com.gscorp.dv1.roles.application.RoleService;
import com.gscorp.dv1.users.application.UserService;
import com.gscorp.dv1.users.web.dto.UserTableDto;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/private/users")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    private final ClientService clientService;
    private final RoleService roleService;
    private final EmployeeService employeeService;

    @GetMapping("/table-view")
    public String getUsersTableView(
            Model model,
            Authentication authentication,
            @RequestParam(required = false) String q,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "100") int size 
        ) {

        Long userId = userService.getUserIdFromAuthentication(authentication);
        if (userId == null) {
            // no autenticado: redirigir al login o devolver error según tu política
            return "redirect:/login";
        }

        // Normalizar page/size (Spring Data usa 0-based)
        int safePage = Math.max(0, page);
        int safeSize = Math.min(Math.max(5, size), 200); // límites: min 5, max 200

        // Normalizar q
        String safeQ = (q == null || q.trim().isEmpty()) ? null : q.trim();

        Page<UserTableDto> usersPage =
                userService.getUserTable(safeQ, safePage, safeSize);

        model.addAttribute("usersPage", usersPage);
        model.addAttribute("qVar", safeQ);

        return "private/users/views/users-table-view";
    }

    @GetMapping("/invite-user")
    public String getInviteUserView(Model model) {
        model.addAttribute("users", userService.findAllWithRolesAndClients());
        model.addAttribute("clients", clientService.getAllClients());
        model.addAttribute("roles", roleService.getAllRoles());
        model.addAttribute("employees", employeeService.findAllUnassignedEmployees());
        return "private/users/views/invite-user-view";
    }



    @GetMapping("/show/{id}")
    public String showUser(@PathVariable Long id, Model model){
        var user = userService.findWithRolesAndClientsById(id);
        model.addAttribute("user", user);
        return "private/users/views/view-user-view";
    }



    @GetMapping("/edit/{id}")
    public String editUser(@PathVariable Long id, Model model){
        var user = userService.findWithRolesAndClientsById(id);
        model.addAttribute("user", user);
        return "private/users/views/edit-user-view";
    }



}
