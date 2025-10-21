package com.gscorp.dv1.bank.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gscorp.dv1.bank.application.BankService;
import com.gscorp.dv1.bank.infrastructure.Bank;
import com.gscorp.dv1.bank.web.dto.BankDto;
import com.gscorp.dv1.bank.web.dto.CreateBankRequest;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/banks")
@RequiredArgsConstructor
public class BankRestController {

    private final BankService bankService;

    @PostMapping("/create")
    public ResponseEntity<BankDto> createBank(
        @jakarta.validation.Valid @RequestBody CreateBankRequest req,
        org.springframework.web.util.UriComponentsBuilder ucb) {
        var entity = Bank.builder()
            .name(req.name().trim())
            .code(req.code())
            .logoUrl(req.logoUrl())
            .active(Boolean.TRUE.equals(req.active()))
            .build();
        var saved = bankService.saveBank(entity);  // que devuelva el guardado
        var location = ucb.path("/api/banks/{id}").buildAndExpand(saved.getId()).toUri();

        var dto = new BankDto(
            saved.getId(),
            saved.getName(),
            saved.getCode(),
            saved.getLogoUrl(),
            saved.getActive());

        return ResponseEntity.created(location).body(dto);
    }

}
