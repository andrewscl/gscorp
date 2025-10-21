package com.gscorp.dv1.bank.application;

import com.gscorp.dv1.bank.infrastructure.Bank;

public interface BankService {

    Bank findById(Long id);
    
}
