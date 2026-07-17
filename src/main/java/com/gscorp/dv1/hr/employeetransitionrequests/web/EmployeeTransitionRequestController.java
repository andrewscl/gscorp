package com.gscorp.dv1.hr.employeetransitionrequests.web;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.gscorp.dv1.config.security.SecurityUser;
import com.gscorp.dv1.enums.EmployeeStatus;
import com.gscorp.dv1.enums.EmployeeTransitionRequestStatus;
import com.gscorp.dv1.enums.TerminationReason;
import com.gscorp.dv1.hr.employees.application.EmployeeService;
import com.gscorp.dv1.hr.employeetransitionrequests.application.EmployeeTransitionRequestService;
import com.gscorp.dv1.hr.employeetransitionrequests.web.dto.EmployeeTransitionRequestDto;

import lombok.AllArgsConstructor;

@Controller
@RequestMapping("/private/employees")
@AllArgsConstructor
public class EmployeeTransitionRequestController {

    private final EmployeeTransitionRequestService employeeTransitionRequestService;
    private final EmployeeService employeeService;

    @GetMapping("/transition-requests/list")
    public String getEmployeeTransitionRequestView (
            Model model,
            @AuthenticationPrincipal SecurityUser securityUser,
            @RequestParam(required = false) EmployeeTransitionRequestStatus status,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "100") int size
    ){
        if(securityUser == null) return "redirect:/login";
        UUID externalId = securityUser.getUser().getExternalId();

        Page<EmployeeTransitionRequestDto> transitionRequests =
            employeeTransitionRequestService
                .getTransitionRequestTable(externalId, status, page, size);

        model.addAttribute("transitionRequestsPage", transitionRequests);
        model.addAttribute("transitionRequests", transitionRequests.getContent());
        model.addAttribute("transitionRequestStatus", EmployeeTransitionRequestStatus.values());
        model.addAttribute("currentStatus", null);
        return "private/hr/transition-requests/views/transition-requests-list";
    }


    @GetMapping("/transition-requests/create-termination-request")
    public String getCreateEmployeeTransitionRequest(
                Model model,
                @AuthenticationPrincipal SecurityUser securityUser
    ){
        if(securityUser == null) return "redirect:/login";

        model.addAttribute("terminationReasons", TerminationReason.values());
        model.addAttribute("activeEmployees", employeeService.findByStatus(EmployeeStatus.ACTIVE));
        return "private/hr/transition-requests/fragments/create-termination-request";
    }

}
