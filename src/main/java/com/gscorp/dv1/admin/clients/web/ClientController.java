package com.gscorp.dv1.admin.clients.web;

import java.util.List;
import java.util.UUID;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gscorp.dv1.admin.clients.application.ClientService;
import com.gscorp.dv1.admin.clients.web.dto.ClientWithCompanyDto;
import com.gscorp.dv1.admin.companies.application.CompanyService;
import com.gscorp.dv1.config.security.SecurityUser;
import com.gscorp.dv1.enums.CompanyStatus;

import lombok.AllArgsConstructor;

@Controller
@RequestMapping("/private/clients")
@AllArgsConstructor
public class ClientController {

    private final ClientService clientService;
    private final CompanyService companyService;

    @GetMapping("/create")
    public String createClient(
            @AuthenticationPrincipal SecurityUser securityUser,
            Model model) {
        if(securityUser == null) return "redirect:/login";
        UUID userExternalId = securityUser.getUser().getExternalId();
        model.addAttribute("companies", companyService
                    .findCompaniesByUserExternalIdAndStatus(userExternalId, CompanyStatus.ACTIVE));
        return "private/clients/fragments/create-client";
    }

    @GetMapping("/dashboard")
    public String getClientsDashboard(Model model) {
        model.addAttribute("clients", clientService.getAllClients());
        return "private/clients/dashboards/templates/clients-dashboard";
    }

    @GetMapping
    public String getClientsTableView(Model model) {
        List<ClientWithCompanyDto> clients = clientService.getAllClientsWithCompany();
        model.addAttribute("clients", clients);
        return "private/clients/views/clients-list";
    }

    @GetMapping("/show/{externalId}")
    public String showClient(@PathVariable UUID externalId, Model model){
        model.addAttribute("client",
            clientService.getClientWithCompanyByExternalId(externalId));
        return "private/clients/views/view-client-view";
    }

    @GetMapping("/edit/{externalId}")
    public String editClient(@PathVariable UUID externalId, Model model){
        model.addAttribute("client",
            clientService.getClientWithCompanyByExternalId(externalId));
        return "private/clients/views/edit-client-view";
    }

}
