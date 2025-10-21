package com.gscorp.dv1.bank.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gscorp.dv1.bank.infrastructure.Bank;
import com.gscorp.dv1.bank.infrastructure.BankRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BankServiceImpl implements BankService{

    private final BankRepository bankRepository;
    
    @Override
    @Transactional(readOnly = true)
    public Bank findById(Long id) {
        return bankRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Banco no encontrado"));
    }

}
