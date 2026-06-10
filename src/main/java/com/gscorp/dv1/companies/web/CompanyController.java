package com.gscorp.dv1.companies.web;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.gscorp.dv1.companies.application.CompanyService;
import com.gscorp.dv1.companies.web.dto.CompanyTableDto;
import com.gscorp.dv1.enums.CompanyStatus;
import com.gscorp.dv1.users.application.UserService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/private/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final UserService userService;
    private final CompanyService companyService;

    @GetMapping("/table-view")
    public String getCompaniesTableView(
        Model model,
        Authentication authentication,
        @RequestParam(required = false) String q,
        @RequestParam(required = false) CompanyStatus status,
        @RequestParam(required = false, defaultValue = "0") int page,
        @RequestParam(required = false, defaultValue = "100") int size
    ) {

        Long userId = userService.getUserIdFromAuthentication(authentication);
        if (userId == null) {
            return "redirect:/login";
        }

        Page<CompanyTableDto> companiesPage =
            companyService.searchCompaniesTableByUserId(userId, q, status, page, size);

        model.addAttribute("companiesPage", companiesPage);
        model.addAttribute("status", status);
        model.addAttribute("qVar", q);
        model.addAttribute("companyStatus", CompanyStatus.values());
        model.addAttribute("count", companiesPage.getTotalElements());
        return "private/companies/views/companies-list";
    }

    @GetMapping("/create")
    public String createCompany (
            Model model,
            Authentication authentication) {

        Long userId = userService.getUserIdFromAuthentication(authentication);
                if (userId == null) {
                return "redirect:/login";
        }
        return "private/companies/views/create-company-view";        
    }

    @GetMapping("/show/{externalId}")
    public String showCompany(
                    @PathVariable UUID externalId,
                    Model model){

        model.addAttribute("company",
                companyService.findCompanyDtoByExternalId(externalId));

        return "private/companies/views/view-company-view";
    }

}
