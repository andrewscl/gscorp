package com.gscorp.dv1.employees.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gscorp.dv1.employees.application.EmployeeService;

import lombok.AllArgsConstructor;

@Controller
@RequestMapping("/private/employees")
@AllArgsConstructor
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;
    
    @GetMapping("/dashboard")
    public String getPrivateDashboardView (Model model) {
        return "private/employee/views/employee-dashboard-view";
    }

    @GetMapping("/table-view")
    public String getEmployeesTableView (Model model) {
        model.addAttribute("employees", employeeService.findAll());
        return "private/employees/views/employees-table-view";
    }

    @GetMapping("/show/{id}")
    public String showEmployee(@PathVariable Long id, Model model){
        var user = employeeService.findByIdWithUserAndProjects(id);
        model.addAttribute("user", user);
        return "private/users/views/view-user-view";
    }

    @GetMapping("/edit/{id}")
    public String editUser(@PathVariable Long id, Model model){
        var user = employeeService.findByIdWithUserAndProjects(id);
        model.addAttribute("user", user);
        return "private/users/views/edit-user-view";
    }

}
