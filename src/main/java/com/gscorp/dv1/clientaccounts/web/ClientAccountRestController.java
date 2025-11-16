package com.gscorp.dv1.clientaccounts.web;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gscorp.dv1.clientaccounts.application.ClientAccountService;
import com.gscorp.dv1.clientaccounts.web.dto.ClientAccountDto;
import com.gscorp.dv1.clientaccounts.web.dto.CreateClientAccountRequest;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/client-accounts")
@RequiredArgsConstructor
public class ClientAccountRestController {

    private final ClientAccountService clientAccountService;

    @PostMapping("/create")
    public ResponseEntity<ClientAccountDto> createClientAccount(
        @jakarta.validation.Valid @RequestBody CreateClientAccountRequest req,
        Authentication authentication,
        org.springframework.web.util.UriComponentsBuilder ucb) {

            ClientAccountDto created = clientAccountService.createClientAccountForPrincipal(req, authentication);
            URI location = ucb.path("/api/client-accounts/{id}")
                .buildAndExpand(created.id())
                .toUri();

        return ResponseEntity.created(location).body(created);
    }

}
