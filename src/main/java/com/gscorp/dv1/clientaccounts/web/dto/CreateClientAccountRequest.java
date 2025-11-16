package com.gscorp.dv1.clientaccounts.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateClientAccountRequest (
    @NotBlank
    String name,
    @NotNull
    Long clientId,
    String notes
){
    
}
