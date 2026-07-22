package com.gscorp.dv1.hr.employeeterminations.web;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.gscorp.dv1.config.security.SecurityUser;
import com.gscorp.dv1.configuration.hrdocuments.application.HrDocumentTypeService;
import com.gscorp.dv1.configuration.hrdocuments.web.dto.HrDocumentTypeDto;
import com.gscorp.dv1.enums.EmployeeStatus;
import com.gscorp.dv1.enums.EmployeeTransitionStatus;
import com.gscorp.dv1.enums.HrProcessType;
import com.gscorp.dv1.enums.TerminationReason;
import com.gscorp.dv1.hr.employees.application.EmployeeService;
import com.gscorp.dv1.hr.employeeterminations.application.EmployeeTerminationService;
import com.gscorp.dv1.hr.employeeterminations.web.dto.EmployeeTerminationDto;
import com.gscorp.dv1.hr.hrdocs.application.HumanResourcesDocumentService;
import com.gscorp.dv1.hr.hrdocs.web.dto.HumanResourcesDocumentDto;

import lombok.AllArgsConstructor;

@Controller
@RequestMapping("/private/employee-terminations")
@AllArgsConstructor
public class EmployeeTerminationController {

    private final EmployeeTerminationService employeeTerminationService;
    private final EmployeeService employeeService;
    private final HrDocumentTypeService hrDocumentTypeService;
    private final HumanResourcesDocumentService humanResourcesDocumentService;

    @GetMapping("/list")
    public String getEmployeeTransitionRequestView (
            Model model,
            @AuthenticationPrincipal SecurityUser securityUser,
            @RequestParam(required = false) EmployeeTransitionStatus status,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "100") int size
    ){
        if(securityUser == null) return "redirect:/login";
        UUID externalId = securityUser.getUser().getExternalId();

        Page<EmployeeTerminationDto> terminationRequests =
            employeeTerminationService
                .getEmployeeTerminationsTable(externalId, status, page, size);

        model.addAttribute("terminationRequestsPage", terminationRequests);
        model.addAttribute("terminationRequests", terminationRequests.getContent());
        model.addAttribute("transitionRequestStatus", EmployeeTransitionStatus.values());
        model.addAttribute("currentStatus", null);
        return "private/hr/termination-requests/views/termination-requests-list";
    }

    @GetMapping("/create-termination")
    public String getCreateEmployeeTransitionRequest(
                Model model,
                @AuthenticationPrincipal SecurityUser securityUser
    ){
        if(securityUser == null) return "redirect:/login";

        PageRequest pageable = PageRequest.of(0, 100);
        Page<HrDocumentTypeDto> hrDocumentTypes =
                    hrDocumentTypeService
                        .findByStatusAndProcess(EmployeeStatus.ACTIVE, HrProcessType.TERMINATION, pageable);

        model.addAttribute("terminationReasons", TerminationReason.values());
        model.addAttribute("activeEmployees", employeeService.findByStatus(EmployeeStatus.ACTIVE));
        model.addAttribute("hrDocumentTypes", hrDocumentTypes.getContent());
        return "private/hr/termination-requests/fragments/create-termination-request";
    }

    @GetMapping("/manage/{externalId}")
    public String getManageEmployeeTransitionRequest(
                Model model,
                @PathVariable UUID externalId,
                @AuthenticationPrincipal SecurityUser securityUser
    ){
        if(securityUser == null) return "redirect:/login";

        PageRequest pageable = PageRequest.of(0, 100);
        Page<HrDocumentTypeDto> hrDocumentTypes =
                    hrDocumentTypeService
                        .findByStatusAndProcess(EmployeeStatus.ACTIVE, HrProcessType.TERMINATION, pageable);

        EmployeeTerminationDto employeeTerminationDto =
                employeeTerminationService.findByExternalId(externalId);
        List<HumanResourcesDocumentDto> documents =
                humanResourcesDocumentService.findByEmployeeTerminationExternalId(externalId);

        model.addAttribute("terminationRequest",employeeTerminationDto);
        model.addAttribute("terminationReasons", TerminationReason.values());
        model.addAttribute("hrDocumentTypes", hrDocumentTypes.getContent());
        model.addAttribute("terminationRequestDocuments", documents);
        return "private/hr/termination-requests/fragments/manage-termination-request";
    }

    @GetMapping("/view/{externalId}")
    public String getViewEmployeeTransitionRequest(
                Model model,
                @PathVariable UUID externalId,
                @AuthenticationPrincipal SecurityUser securityUser
    ){
        if(securityUser == null) return "redirect:/login";

        EmployeeTerminationDto employeeTerminationDto =
                employeeTerminationService.findByExternalId(externalId);
        List<HumanResourcesDocumentDto> documents =
                humanResourcesDocumentService.findByEmployeeTerminationExternalId(externalId);

        System.out.println(">>> CONTENIDO DEL DTO: " + employeeTerminationDto);

        model.addAttribute("terminationRequest",employeeTerminationDto);
        model.addAttribute("terminationRequestDocuments", documents);
        return "private/hr/termination-requests/fragments/view-termination-request";
    }

}
