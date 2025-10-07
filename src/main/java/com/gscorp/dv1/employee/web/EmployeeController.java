package com.gscorp.dv1.employee.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.AllArgsConstructor;

@Controller
@RequestMapping("/private/employee")
@AllArgsConstructor
public class EmployeeController {
    
    @GetMapping("/dashboard")
    public String getPrivateDashboardView (Model model) {
        return "private/employee/views/employee/dashboard/view";
    }


}
