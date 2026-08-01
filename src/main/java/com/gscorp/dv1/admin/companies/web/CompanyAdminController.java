package com.gscorp.dv1.admin.companies.web;

import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.gscorp.dv1.admin.companies.application.CompanyService;
import com.gscorp.dv1.admin.companies.web.dto.CompanyTableDto;
import com.gscorp.dv1.config.security.SecurityUser;
import com.gscorp.dv1.enums.CompanyStatus;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin/companies")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMINISTRATOR')")
public class CompanyAdminController {

    private final CompanyService companyService;
    
    @GetMapping
    public String getAdminCompaniesList(
        Model model,
        @RequestParam(required = false) String q,
        @RequestParam(required = false) CompanyStatus status,
        @RequestParam(required = false, defaultValue = "0") int page,
        @RequestParam(required = false, defaultValue = "100") int size
    ) {
        Page<CompanyTableDto> companiesPage =
            companyService.getAllCompaniesTableForAdmin(page, size);
        model.addAttribute("companiesPage", companiesPage);
        model.addAttribute("status", status);
        model.addAttribute("qVar", q);
        model.addAttribute("companyStatus", CompanyStatus.values());
        model.addAttribute("count", companiesPage.getTotalElements());
        return "private/admin/companies/companies-list";
    }

    @GetMapping("/create")
    public String createCompany (
            Model model,
            @AuthenticationPrincipal SecurityUser securityUser
    ) {
        if(securityUser == null) return "redirect:/login";
        return "private/admin/companies/create-company";        
    }

}
