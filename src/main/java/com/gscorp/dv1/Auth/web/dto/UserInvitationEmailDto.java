package com.gscorp.dv1.auth.web.dto;

public record UserInvitationEmailDto (
    String email,
    String username,
    String displayName,
    String token
){}
