package com.gscorp.dv1.operations.dashboard.web;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.gscorp.dv1.attendance.application.AttendanceService;
import com.gscorp.dv1.attendance.web.dto.dashboard.AttendanceCardState;
import com.gscorp.dv1.attendance.web.dto.dashboard.DashboardHeaderInfo;
import com.gscorp.dv1.components.ZoneResolver;
import com.gscorp.dv1.config.security.SecurityUser;
import com.gscorp.dv1.hr.employees.application.EmployeeService;
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
    private ZoneResolver zoneResolver;

    @GetMapping("/ops-dashboard")
    public String getOpsDashboardView(Model model) {
        return "private/ops/dashboards/templates/ops-dashboard";
    }

    @GetMapping("/dashboards/security-operator")
    public String getPrivateDashboardView (
            Model model,
            @AuthenticationPrincipal SecurityUser securityUser,
            @RequestParam(required = false) String tz
        ) {

        UUID externalId = securityUser.getUser().getExternalId();
        UserViewDto userViewDto = userService.findWithCompaniesAndClientsByExternalId(externalId);
        var employee = employeeService.findByIdViewEmployee(userViewDto.employeeId());

        ZoneId zoneId = zoneResolver.resolveZone(externalId, tz).zoneId();
        LocalDateTime now = LocalDateTime.now(zoneId);
        String formattedDate = now
                .format(DateTimeFormatter
                .ofPattern("EEEE, dd 'de' MMMM", new Locale("es", "ES")));

        DashboardHeaderInfo header = attendanceService.getDashboardHeader(externalId, tz);
        AttendanceCardState card = attendanceService.getAttendanceCardState(externalId, tz);
        System.out.println("Dashboard Header Info: " + header);
        System.out.println("Attendance Card State: " + card);

            model.addAttribute("employee", employee);
            model.addAttribute("currentDate", formattedDate);
            model.addAttribute("lastPunchText", card.lastPunchText());
            model.addAttribute("nextAction", card.nextAction());
            model.addAttribute("greeting", header.greeting());
            model.addAttribute("emoji", header.emoji());
            model.addAttribute("message", card.statusText());
            model.addAttribute("attendanceCardState", card);
        return "private/ops/dashboards/templates/ops-operator-dashboard";
    }

    

}
