package com.gscorp.dv1.employees.web;

import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gscorp.dv1.employees.application.EmployeeService;
import com.gscorp.dv1.employees.application.EmployeeTabsServiceImpl;
import com.gscorp.dv1.employees.web.dto.EmployeeViewDto;
import com.gscorp.dv1.users.application.UserService;

import lombok.AllArgsConstructor;

@Controller
@RequestMapping("/private/employees")
@AllArgsConstructor
public class EmployeeTabController {

    private EmployeeService employeeService;
    private EmployeeTabsServiceImpl employeeTabsService;
    private UserService userService;

    @GetMapping("/view/{externalId}")
    public String viewEmployee(
            @PathVariable UUID externalId,
            Model model,
            Authentication authentication
        ){

        Long userId = userService.getUserIdFromAuthentication(authentication);
        if (userId == null) {
            return "redirect:/login";
        }

        EmployeeViewDto employee =
                employeeService.findByExternalIdViewEmployee(externalId);
        if (employee == null) {
            return "redirect:/private/employees";
        }
        
        model.addAttribute("employee", employee);
        model.addAttribute("employeeTabs", employeeTabsService.getTabs());
        return "private/employees/fragments/view-employee";
    }

}
