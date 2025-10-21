package com.gscorp.dv1.bank.web.dto;

import com.gscorp.dv1.bank.infrastructure.Bank;

public record BankDto (
    Long id,
    String name,
    Long code,
    String logoUrl,
    Boolean active
) {
    public static BankDto fromEntity(Bank b) {
        if (b == null) return null;
        return new BankDto(
            b.getId(),
            b.getName(),
            b.getCode(),
            b.getLogoUrl(),
            b.getActive()
        );
    }
}
