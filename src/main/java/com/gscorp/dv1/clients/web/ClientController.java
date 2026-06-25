package com.gscorp.dv1.clients.web;

import java.util.List;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gscorp.dv1.clients.application.ClientService;
import com.gscorp.dv1.clients.web.dto.ClientWithCompanyDto;
import com.gscorp.dv1.companies.application.CompanyService;
import com.gscorp.dv1.users.application.UserService;

import lombok.AllArgsConstructor;

@Controller
@RequestMapping("/private/clients")
@AllArgsConstructor
public class ClientController {

    private final ClientService clientService;
    private final UserService userService;
    private final CompanyService companyService;

    @GetMapping("/create")
    public String createClient(
            Authentication authentication,
            Model model) {

        Long userId = userService.getUserIdFromAuthentication(authentication);
                if (userId == null) {
                return "redirect:/login";
        }

        model.addAttribute("companies"
                , companyService.getAllCompaniesForSelect());

        return "private/clients/views/create-client-view";
    }

    @GetMapping("/dashboard")
    public String getClientsDashboard(Model model) {
        model.addAttribute("clients", clientService.getAllClients());
        return "private/clients/dashboards/templates/clients-dashboard";
    }

    @GetMapping("/table-view")
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
