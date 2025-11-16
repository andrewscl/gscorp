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

import lombok.AllArgsConstructor;

@Controller
@RequestMapping("/private/client-accounts")
@AllArgsConstructor
public class ClientAccountController {

    ClientAccountService clientAccountService;
    
    @GetMapping("/table-view")
    public String getClientAccountsTableView(Model model, Authentication authentication) {
        // El servicio ofrece una variante que resuelve el userId desde Authentication internamente
        List<ClientAccountDto> accounts = clientAccountService.findAccountDtosForPrincipal(authentication);

        // Aseguramos que siempre haya una lista (no null) en el model
        model.addAttribute("accounts", accounts == null ? Collections.emptyList() : accounts);

        // Vista Thymeleaf que renderiza la tabla (ajusta la ruta si tu estructura difiere)
        return "private/client-accounts/views/client-accounts-table-view";

    }

}
