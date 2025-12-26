package com.gscorp.dv1.services;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import com.gscorp.dv1.auth.AuthRequest;
import com.gscorp.dv1.auth.AuthResponse;
import com.gscorp.dv1.security.JwtService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SignInService {
    
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserDetailsServiceImpl userDetailsServiceImpl;

    public AuthResponse signin(AuthRequest request) {
        //Autenticación
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getUsername(),
                request.getPassword())
            );

        var userDetails = userDetailsServiceImpl
                            .loadUserByUsername(request.getUsername());
        var jwtToken = jwtService.generateToken(userDetails);

        return new AuthResponse(jwtToken);

    }
}
