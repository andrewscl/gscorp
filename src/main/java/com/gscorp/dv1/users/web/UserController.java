package com.gscorp.dv1.users.web;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.gscorp.dv1.clients.application.ClientService;
import com.gscorp.dv1.clients.web.dto.ClientSelectDto;
import com.gscorp.dv1.companies.application.CompanyService;
import com.gscorp.dv1.employees.application.EmployeeService;
import com.gscorp.dv1.employees.web.dto.EmployeeSelectDto;
import com.gscorp.dv1.enums.UserStatus;
import com.gscorp.dv1.roles.application.RoleService;
import com.gscorp.dv1.roles.web.dto.RoleSelectDto;
import com.gscorp.dv1.users.application.UserService;
import com.gscorp.dv1.users.web.dto.UserTableDto;
import com.gscorp.dv1.users.web.dto.UserViewDto;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/private/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final CompanyService companyService;
    private final ClientService clientService;
    private final RoleService roleService;
    private final EmployeeService employeeService;


    @GetMapping("/table-view")
    public String getUsersTableView(
            Model model,
            Authentication authentication,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "100") int size 
        ) {

        Long userId = userService.getUserIdFromAuthentication(authentication);
        if (userId == null) {
            return "redirect:/login";
        }
        Page<UserTableDto> usersPage =
                userService.getAllUsersWithEmployee(page, size);

        model.addAttribute("usersPage", usersPage);
        model.addAttribute("userStatus", UserStatus.values());
        model.addAttribute("count",usersPage.getTotalElements());

        return "private/users/views/users-list";
    }


    @GetMapping("/invite")
    public String getInviteUserView(Model model) {
        model.addAttribute("companies", companyService.getAllCompaniesForSelect());
        model.addAttribute("clients", clientService.getAllClients());
        model.addAttribute("roles", roleService.getAllRoles());
        model.addAttribute("employees", employeeService.findAllUnassignedEmployees());
        return "private/users/views/invite-user-view";
    }

    @GetMapping("/show/{id}")
    public String showUser(@PathVariable Long id, Model model){
        UserViewDto userDto = userService.findWithCompaniesAndClientsById(id);
        List<RoleSelectDto> roles = roleService.getAllRolesSelectDto();
        List<ClientSelectDto> clients = clientService.getAllClientsSelectDto();
        List<EmployeeSelectDto> employees = employeeService.getAllEmployeesSelectDto();
        model.addAttribute("user", userDto);
        model.addAttribute("roles", roles);
        model.addAttribute("clients", clients);
        model.addAttribute("employees", employees);
        return "private/users/views/view-user-view";
    }

    @GetMapping("/edit/{id}")
    public String editUser(@PathVariable Long id, Model model){

        UserViewDto userDto = userService.findWithCompaniesAndClientsById(id);
        List<RoleSelectDto> roles = roleService.getAllRolesSelectDto();
        List<ClientSelectDto> clients = clientService.getAllClientsSelectDto();
        List<EmployeeSelectDto> employees = employeeService.getAllEmployeesSelectDto();
        model.addAttribute("user", userDto);
        model.addAttribute("roles", roles);
        model.addAttribute("clients", clients);
        model.addAttribute("employees", employees);
        model.addAttribute("userStatusList", UserStatus.values());

        return "private/users/views/edit-user-view";
    }


    @GetMapping("/table-search")
    public String getUserTableSearch(
        Model model,
        Authentication authentication,
        @RequestParam(required = false, defaultValue = "") String q,
        @RequestParam(required = false) UserStatus status,
        @RequestParam(required = false, defaultValue = "0") int page,
        @RequestParam(required = false, defaultValue = "100") int size){

        Long userId = userService.getUserIdFromAuthentication(authentication);
        if (userId == null) {
            return "redirect:/login";
        }

        Page<UserTableDto> usersPage =
                userService.searchUsersWithEmployee(q, status, page, size);

        model.addAttribute("usersPage", usersPage);
        model.addAttribute("status", status);
        model.addAttribute("qVar", q);
        model.addAttribute("userStatus", UserStatus.values());
        model.addAttribute("count", usersPage.getTotalElements());

        return "private/users/fragments/users-table-partial :: partial";
    }

}
