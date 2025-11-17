package com.gscorp.dv1.shiftrequests.web;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.gscorp.dv1.clientaccounts.application.ClientAccountService;
import com.gscorp.dv1.clientaccounts.web.dto.ClientAccountDto;
import com.gscorp.dv1.shiftrequests.application.ShiftRequestService;
import com.gscorp.dv1.shiftrequests.web.dto.CreateShiftRequest;
import com.gscorp.dv1.shiftrequests.web.dto.ShiftRequestDto;
import com.gscorp.dv1.users.application.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/shift-requests")
@RequiredArgsConstructor
public class ShiftRequestRestController {

    private final ShiftRequestService shiftRequestService;
    private final UserService userService;
    private final ClientAccountService clientAccountService;

    @PostMapping("/create")
    public ResponseEntity<ShiftRequestDto> createShiftRequest(
        @jakarta.validation.Valid @RequestBody CreateShiftRequest req,
        Authentication authentication,
        UriComponentsBuilder ucb) {

        // delega en el service que valida permisos y crea la entidad
        ShiftRequestDto dto = shiftRequestService.createShiftRequestForPrincipal(req, authentication);

        Long id = dto != null ? dto.id() : null;

        if (id != null) {
            URI uri = ucb.path("/api/shift-requests/{id}").buildAndExpand(id).toUri();
            return ResponseEntity.created(uri).body(dto);
        } else {
            // si no tenemos id exponible, devolvemos 201 con body sin Location
            return ResponseEntity.status(HttpStatus.CREATED).body(dto);
        }

    }

    @PutMapping("/edit/{id}")
    public ResponseEntity<ShiftRequestDto> editShiftRequest(
        @PathVariable Long id,
        @jakarta.validation.Valid @RequestBody CreateShiftRequest req
    ) {
        Optional<ShiftRequestDto> updatedDtoOpt = shiftRequestService.update(id, req);

        return updatedDtoOpt
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/sites/{siteId}/accounts")
    public ResponseEntity<List<ClientAccountDto>> getClientAccountsForSite(
                @PathVariable ("siteId") Long siteId,
                Authentication authentication ) {
        
        Long userId = userService.getUserIdFromAuthentication(authentication);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<ClientAccountDto> accounts = clientAccountService.getClientAccountsForSite(siteId, userId);

        return ResponseEntity.ok(accounts);
    }


}   
    
