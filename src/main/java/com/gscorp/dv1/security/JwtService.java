package com.gscorp.dv1.security;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.Claims;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class JwtService {
    
    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    private Key secretKey; 

    @PostConstruct
    public void initSecretKey () {
        secretKey = Keys.hmacShaKeyFor(
            jwtSecret.getBytes(StandardCharsets.UTF_8)
        );
    }

    public String generateToken (UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        //agrega la claims de roles
        claims.put("roles", userDetails.getAuthorities().stream()
            .map(authority -> authority.getAuthority())
            .collect(Collectors.toList())
            );
        System.out.println("Authorities al generar el token: " + 
                                userDetails.getAuthorities());
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 
                                            jwtExpiration))
                .signWith(secretKey)
                .compact();
    }

    //Metodo para extraer las claims (Necesario para leer los roles despues)
    public Claims extractAllClaims (String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String extractUsername (String token) {
        return extractAllClaims (token).getSubject();
    }

    public Date extractExpiration (String token) {
        return extractAllClaims (token).getExpiration();
    }

    public boolean isTokenExpired (String token) {
        return extractExpiration(token).before(new Date());
    }

    public boolean isTokenValid (String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token)); 
    }

    public boolean isTokenValid(String token) {
        String username = extractUsername(token);
        return (username != null && !isTokenExpired(token));
    }

}
