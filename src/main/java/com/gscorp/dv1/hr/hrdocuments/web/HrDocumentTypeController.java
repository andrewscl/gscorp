package com.gscorp.dv1.hr.hrdocuments.web;

import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.gscorp.dv1.config.security.SecurityUser;
import com.gscorp.dv1.enums.EmployeeTransitionStatus;
import com.gscorp.dv1.hr.hrdocuments.application.HrDocumentTypeService;
import com.gscorp.dv1.hr.hrdocuments.web.dto.HrDocumentTypeDto;

import lombok.AllArgsConstructor;

@Controller
@RequestMapping("/private/hr-document-types")
@AllArgsConstructor
public class HrDocumentTypeController {

    private final HrDocumentTypeService hrDocumentTypeService;

    @GetMapping("/list")
    public String getEmployeeTransitionRequestView (
            Model model,
            @AuthenticationPrincipal SecurityUser securityUser,
            @RequestParam(required = false) EmployeeTransitionStatus status,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "100") int size
    ){
        if(securityUser == null) return "redirect:/login";

        Page<HrDocumentTypeDto> hrDocumentTypes =
                    hrDocumentTypeService
                        .findByStatusAndProcess(null, null);

        model.addAttribute("hrDocumentTypesPage", hrDocumentTypes);
        model.addAttribute("hrDocumentTypes", hrDocumentTypes.getContent());
        return "private/configuration/hr-document-types/views/hr-document-types-list";
    }


}
