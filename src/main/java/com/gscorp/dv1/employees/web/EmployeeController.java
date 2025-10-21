package com.gscorp.dv1.employees.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gscorp.dv1.employees.application.EmployeeService;
import com.gscorp.dv1.enums.BankAccountType;
import com.gscorp.dv1.enums.ContractType;
import com.gscorp.dv1.enums.Gender;
import com.gscorp.dv1.enums.HealthSystem;
import com.gscorp.dv1.enums.MaritalStatus;
import com.gscorp.dv1.enums.PaymentMethod;
import com.gscorp.dv1.enums.PrevitionalSystem;
import com.gscorp.dv1.enums.ShiftSystem;
import com.gscorp.dv1.enums.StudyLevel;
import com.gscorp.dv1.enums.WorkSchedule;
import com.gscorp.dv1.projects.application.ProjectService;
import com.gscorp.dv1.users.application.UserService;

import lombok.AllArgsConstructor;

@Controller
@RequestMapping("/private/employees")
@AllArgsConstructor
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private UserService userService;
    
    @GetMapping("/dashboard")
    public String getPrivateDashboardView (Model model) {
        return "private/employee/views/employee-dashboard-view";
    }

    @GetMapping("/table-view")
    public String getEmployeesTableView (Model model) {
        model.addAttribute("employees", employeeService.findAll());
        return "private/employees/views/employees-table-view";
    }

    @GetMapping("/create")
    public String getCreateEmployeeView (Model model) {
        model.addAttribute("projects", projectService.findAll());
        model.addAttribute("users", userService.findAll());
        model.addAttribute("genders", Gender.values());
        model.addAttribute("maritalStatuses", MaritalStatus.values());
        model.addAttribute("studyLevels", StudyLevel.values());
        model.addAttribute("previsionalSystems", PrevitionalSystem.values());
        model.addAttribute("healthSystems", HealthSystem.values());
        model.addAttribute("paymentMethods", PaymentMethod.values());
        model.addAttribute("bankAccountsTypes", BankAccountType.values());
        model.addAttribute("contractTypes", ContractType.values());
        model.addAttribute("workSchedules", WorkSchedule.values());
        model.addAttribute("shiftSystems", ShiftSystem.values());
        

        return "private/employees/views/create-employee-view";
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
