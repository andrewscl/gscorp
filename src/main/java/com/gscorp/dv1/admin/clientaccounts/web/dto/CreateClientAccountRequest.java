package com.gscorp.dv1.admin.clientaccounts.web.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateClientAccountRequest (
    @NotBlank
    String name,

    @NotNull
    UUID projectExternalId,
    
    String notes
){
    
}
