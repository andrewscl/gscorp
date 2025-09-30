package com.gscorp.dv1.security;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.gscorp.dv1.users.infrastructure.User;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class SecurityUser implements UserDetails {

    private final User user;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        if ( user.getRoles() == null){
            System.out.println("El usuario no tiene roles asignados (null).");
            return List.of(); // devuelve una lista vacía
        }

        // Crear una copia segura para evitar ConcurrentModificationException
        List<String> roles = user.getRoles().stream()
            .map(r -> r.getRole())
            .collect(Collectors.toList());

        return roles.stream()
        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
        .collect(Collectors.toList());
    }

        @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

     @Override
    public boolean isAccountNonExpired() {
        return true; // o según tu lógica
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // o según tu lógica
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // o según tu lógica
    }

    @Override
    public boolean isEnabled() {
        return true; // o según tu lógica
    }
    
}
