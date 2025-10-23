package com.gscorp.dv1.users.web.dto;

public record SetPasswordRequest (
    String password,
    String token
){
    
}
