package com.gscorp.dv1.operations.web;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gscorp.dv1.attendance.application.AttendanceService;
import com.gscorp.dv1.attendance.web.dto.DashboardHeaderInfo;
import com.gscorp.dv1.employees.application.EmployeeService;
import com.gscorp.dv1.security.SecurityUser;
import com.gscorp.dv1.users.application.UserService;
import com.gscorp.dv1.users.web.dto.UserViewDto;

import lombok.AllArgsConstructor;

@Controller
@RequestMapping("/private/operations")
@AllArgsConstructor
public class OperationsController {

    private UserService userService;
    private EmployeeService employeeService;
    private AttendanceService attendanceService;

    @GetMapping("/dashboards/analyst")
    public String getAnalystDashboardView(Model model) {
        return "private/ops/dashboards/templates/ops-analyst-dashboard";
    }

    @GetMapping("/dashboards/security-operator")
    public String getPrivateDashboardView (
            Model model,
            Authentication authentication) {

            if(authentication == null || !authentication.isAuthenticated()) {
                return "redirect:/login";
            }

            Object principal = authentication.getPrincipal();
            if(!(principal instanceof SecurityUser)) {
                return "redirect:/login";
            }

            SecurityUser securityUser = (SecurityUser) principal;

            UUID externalId = securityUser.getUser().getExternalId();

            UserViewDto userViewDto = userService.findWithCompaniesAndClientsByExternalId(externalId);

            var employee = employeeService.findByIdViewEmployee(userViewDto.employeeId());

            LocalDateTime now = LocalDateTime.now();
            String formattedDate = now
                .format(DateTimeFormatter
                .ofPattern("EEEE, dd 'de' MMMM",
                                                new Locale("es", "ES")));

            DashboardHeaderInfo dashboardHeaderInfo = attendanceService.getDashboardHeader(externalId);

            model.addAttribute("employee", employee);
            model.addAttribute("currentDate", formattedDate);
            model.addAttribute("lastPunchText", dashboardHeaderInfo.lastPunchText());
            model.addAttribute("nextAction", dashboardHeaderInfo.nextAction());
            model.addAttribute("greeting", dashboardHeaderInfo.greeting());
            model.addAttribute("emoji", dashboardHeaderInfo.emoji());
            model.addAttribute("message", dashboardHeaderInfo.message());
        return "private/ops/dashboards/templates/ops-operator-dashboard";
    }

}
