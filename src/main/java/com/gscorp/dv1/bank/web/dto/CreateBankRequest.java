package com.gscorp.dv1.bank.web.dto;

public record CreateBankRequest (
    String name,
    Long code,
    String logoUrl,
    Boolean active
){
    
}
