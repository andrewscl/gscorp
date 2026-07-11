package com.gscorp.dv1.core.bank.web.dto;

import org.springframework.web.multipart.MultipartFile;

public record CreateBankRequest (
    String name,
    Long code,
    MultipartFile logo,
    Boolean active
){
    
}
