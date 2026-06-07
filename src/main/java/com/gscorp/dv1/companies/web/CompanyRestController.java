package com.gscorp.dv1.companies.web;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.gscorp.dv1.companies.application.CompanyService;
import com.gscorp.dv1.companies.web.dto.CompanyDto;
import com.gscorp.dv1.companies.web.dto.CreateCompanyRequest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
public class CompanyRestController {

    private final CompanyService companyService;

    @PostMapping("/create")
    public ResponseEntity <CompanyDto> createCompany (
        @Valid @RequestBody CreateCompanyRequest req,
        UriComponentsBuilder ucb){

        CompanyDto newCompany = companyService.createCompany(req);

        URI location = ucb
                        .path("/private/companies/{externalId}")
                        .buildAndExpand(newCompany.externalId())
                        .toUri();

        return ResponseEntity.created(location).body(newCompany);
    }

}
