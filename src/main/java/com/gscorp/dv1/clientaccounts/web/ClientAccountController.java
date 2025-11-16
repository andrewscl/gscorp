package com.gscorp.dv1.clientaccounts.web;

import java.util.Collections;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gscorp.dv1.clientaccounts.application.ClientAccountService;
import com.gscorp.dv1.clientaccounts.web.dto.ClientAccountDto;
import com.gscorp.dv1.clients.application.ClientService;
import com.gscorp.dv1.clients.web.dto.ClientDto;
import com.gscorp.dv1.users.application.UserService;

import lombok.AllArgsConstructor;

@Controller
@RequestMapping("/private/client-accounts")
@AllArgsConstructor
public class ClientAccountController {

    private final ClientAccountService clientAccountService;
    private final UserService userService;
    private final ClientService clientService;
    
    
    @GetMapping("/table-view")
    public String getClientAccountsTableView(Model model, Authentication authentication) {
        // El servicio ofrece una variante que resuelve el userId desde Authentication internamente
        List<ClientAccountDto> accounts = clientAccountService.findAccountDtosForPrincipal(authentication);

        // Aseguramos que siempre haya una lista (no null) en el model
        model.addAttribute("accounts", accounts == null ? Collections.emptyList() : accounts);

        // Vista Thymeleaf que renderiza la tabla (ajusta la ruta si tu estructura difiere)
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

}
