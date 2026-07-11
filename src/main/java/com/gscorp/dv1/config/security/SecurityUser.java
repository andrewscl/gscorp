package com.gscorp.dv1.config.security;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

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
        if ( user.getRole() == null){
            System.out.println("El usuario no tiene role asignado (null).");
            return Collections.emptyList();
        }

        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().getRole()));
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
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    //Exponer del id para AuditorAware
    public Long getId() {
        return user != null ? user.getId() : null;
    }

    //Exponer el User si se desea crear desde otros puntos
    public User getUser() {
        return user;
    }
    
}
