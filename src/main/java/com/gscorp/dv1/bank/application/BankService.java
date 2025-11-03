package com.gscorp.dv1.bank.application;

import java.util.Set;

import org.springframework.web.multipart.MultipartFile;

import com.gscorp.dv1.bank.infrastructure.Bank;

public interface BankService {

    Bank findById(Long id);
    Set<Bank> findAll();
    Bank saveBank(Bank bank);
    String storeBankLogo (MultipartFile logoFile);
}
