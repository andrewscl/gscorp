package com.gscorp.dv1.admin.clients;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.gscorp.dv1.admin.clientaccounts.application.ClientAccountService;
import com.gscorp.dv1.admin.clientaccounts.web.dto.ClientAccountDto;
import com.gscorp.dv1.admin.clientaccounts.web.dto.CreateClientAccountRequest;
import com.gscorp.dv1.users.application.UserScopeService;
import com.gscorp.dv1.users.application.dto.ProjectScope;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/client-accounts")
@RequiredArgsConstructor
public class ClientAccountRestController {

    private final ClientAccountService clientAccountService;
    private final UserScopeService userScopeService;

    @PostMapping("/create")
    public ResponseEntity<ClientAccountDto> createClientAccount(
                                        @Valid @RequestBody CreateClientAccountRequest req,
                                        @RequestParam UUID projectExternalId,
                                        UriComponentsBuilder ucb){
        ProjectScope scope = userScopeService.getProjectScope();
        ClientAccountDto created = clientAccountService.createClientAccount(
                                    scope.ignoreFilter(), scope.projectIds(), req, projectExternalId);
        URI location = ucb.path("/{externalId}")
            .buildAndExpand(created.externalId())
            .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping("/site/{siteExternalId}/client-accounts")
    public ResponseEntity<List<ClientAccountDto>> getClientAccountsBySiteExternalId(
            @PathVariable("siteExternalId") UUID siteExternalId){
        ProjectScope scope = userScopeService.getProjectScope();
        List<ClientAccountDto> accounts = clientAccountService.getClientAccountsBySite(
                                            scope.ignoreFilter(), scope.projectIds(), siteExternalId);
        return ResponseEntity.ok(accounts);
    }
}
