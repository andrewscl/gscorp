package com.gscorp.dv1.admin.web;

import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.gscorp.dv1.companies.application.CompanyService;
import com.gscorp.dv1.companies.web.dto.CompanyTableDto;
import com.gscorp.dv1.users.application.UserService;

import lombok.AllArgsConstructor;

@Controller
@RequestMapping("/admin/companies")
@AllArgsConstructor
public class AdminCompanyController {

    private final UserService userService;
    private final CompanyService companyService;

    @GetMapping("/table-view")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public String getCompaniesTableView(
        Model model,
        Authentication authentication,
        @RequestParam(required = false, defaultValue = "0") int page,
        @RequestParam(required = false, defaultValue = "100") int size
    ) {

        Long userId = userService.getUserIdFromAuthentication(authentication);
        if (userId == null) {
            return "redirect:/login";
        }

        Page<CompanyTableDto> companiesPage =
                companyService.getAllCompaniesTableForAdmin(page, size);

        model.addAttribute("companiesPage", companiesPage);
        return "private/companies/views/companies-list";
    }
}
