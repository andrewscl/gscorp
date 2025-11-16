package com.gscorp.dv1.clientaccounts.web;

import java.net.URI;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import com.gscorp.dv1.clientaccounts.application.ClientAccountService;
import com.gscorp.dv1.clientaccounts.web.dto.ClientAccountDto;
import com.gscorp.dv1.clientaccounts.web.dto.CreateClientAccountRequest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/client-accounts")
@RequiredArgsConstructor
public class ClientAccountRestController {

    private final ClientAccountService clientAccountService;

    @PostMapping("/create")
    public ResponseEntity<ClientAccountDto> createClientAccount(
        @Valid @RequestBody CreateClientAccountRequest req,
        Authentication authentication,
        UriComponentsBuilder ucb) {

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado");
        }

        // El service debe:
        //  - extraer userId desde authentication,
        //  - verificar que req.clientId pertenece al usuario,
        //  - crear la entidad y devolver ClientAccountDto.
        ClientAccountDto created = clientAccountService.createClientAccountForPrincipal(req, authentication);

        URI location = ucb.path("/api/client-accounts/{id}")
            .buildAndExpand(created.id())
            .toUri();

        return ResponseEntity.created(location).body(created);
    }

}
