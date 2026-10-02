package com.gscorp.dv1.admin.clientaccounts.web;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gscorp.dv1.admin.clientaccounts.application.ClientAccountService;
import com.gscorp.dv1.admin.clientaccounts.web.dto.ClientAccountDto;
import com.gscorp.dv1.admin.clients.application.ClientService;
import com.gscorp.dv1.admin.clients.web.dto.ClientDto;
import com.gscorp.dv1.config.security.SecurityUser;
import com.gscorp.dv1.users.application.UserScopeService;
import com.gscorp.dv1.users.application.UserService;
import com.gscorp.dv1.users.application.dto.ProjectScope;

import lombok.AllArgsConstructor;

@Controller
@RequestMapping("/private/client-accounts")
@AllArgsConstructor
public class ClientAccountController {

    private final ClientAccountService clientAccountService;
    private final UserService userService;
    private final ClientService clientService;
    private final UserScopeService userScopeService;    

    @GetMapping("/table-view")
    public String getClientAccountsTableView(Model model) {
        ProjectScope scope = userScopeService.getProjectScope();
        List<ClientAccountDto> accounts = clientAccountService.getDtosByUserScope(
                                                            scope.ignoreFilter(), scope.projectIds());
        model.addAttribute("accounts", accounts == null ? Collections.emptyList() : accounts);
        return "private/client-accounts/views/client-accounts-table-view";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model, Authentication authentication) {
        Long userId = userService.getUserIdFromAuthentication(authentication);
        if(userId == null) {
            return "redirect:/login"; // O la vista que corresponda
        }

        // Mejor pasar DTOs con id+name para poblar el <select>
        List<ClientDto> clients = clientService.findDtosByUserId(userId);
        if (clients == null) clients = Collections.emptyList();

        model.addAttribute("clients", clients);

        // UX: si solo hay 1 client, pasar un preseleccionado (opcional)
        if (clients.size() == 1) {
            model.addAttribute("preselectedClientId", clients.get(0).id());
        }
        return "private/client-accounts/views/create-client-account-view";
    }

    @GetMapping("/show/{id}")
    public String showClientAccount(
                            @AuthenticationPrincipal SecurityUser securityUser,
                            @PathVariable Long id,
                            Model model){
        UUID externalId = securityUser.getUser().getExternalId();
        ProjectScope scope = userScopeService.getProjectScope();
        ClientAccountDto account = clientAccountService.getAccountDtoByExternalId(
                                                            scope.ignoreFilter(), 
                                                            scope.projectIds(),
                                                            externalId);  
        model.addAttribute("clientAccount", account);
        return "private/client-accounts/views/view-client-account-view";
    }

    @GetMapping("/edit/{id}")
    public String editClientAccount(
                            @AuthenticationPrincipal SecurityUser securityUser,
                            @PathVariable Long id,
                            Model model){
        UUID externalId = securityUser.getUser().getExternalId();
        ProjectScope scope = userScopeService.getProjectScope();
        ClientAccountDto clientAccount = clientAccountService.getAccountDtoByExternalId(
                                                            scope.ignoreFilter(), 
                                                            scope.projectIds(),
                                                            externalId);
        model.addAttribute("clientAccount", clientAccount);
        return "private/client-accounts/views/edit-client-account-view";
    }

}
