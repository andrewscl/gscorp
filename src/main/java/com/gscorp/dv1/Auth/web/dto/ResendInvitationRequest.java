package com.gscorp.dv1.auth.web.dto;

import java.util.UUID;

public record ResendInvitationRequest (
    UUID employeeExternalId
){
    
}
