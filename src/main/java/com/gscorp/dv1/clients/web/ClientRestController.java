package com.gscorp.dv1.clients.web;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gscorp.dv1.clients.application.ClientService;
import com.gscorp.dv1.clients.infrastructure.Client;
import com.gscorp.dv1.clients.web.dto.ClientDto;
import com.gscorp.dv1.clients.web.dto.CreateClientRequest;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class ClientRestController {

    private final ClientService clientService;

    @PostMapping("/create")
    public ResponseEntity <ClientDto> createClient(
        @jakarta.validation.Valid @RequestBody CreateClientRequest req,
        org.springframework.web.util.UriComponentsBuilder ucb) {
        var entity = Client.builder()
            .name(req.name().trim())
            .legalName(req.legalName())
            .taxId(req.taxId())
            .contactEmail(req.contactEmail())
            .contactPhone(req.contactPhone())
            .active(Boolean.TRUE.equals(req.active()))
            .build();
        var saved = clientService.saveClient(entity);  // que devuelva el guardado
        var location = ucb.path("/api/clients/{id}").buildAndExpand(saved.getId()).toUri();
        return ResponseEntity.created(location).body(
            new ClientDto(saved.getId(), saved.getName(), saved.getTaxId(), saved.getContactEmail(), saved.getActive())
        );
    }

    @GetMapping("/all")
    public ResponseEntity<List<ClientDto>> getAllClients() {
        List<ClientDto> clients = clientService.getAllClients();
        return ResponseEntity.ok(clients);
    }

}
