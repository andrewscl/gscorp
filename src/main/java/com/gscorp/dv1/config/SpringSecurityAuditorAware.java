package com.gscorp.dv1.config;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.gscorp.dv1.security.SecurityUser;
import com.gscorp.dv1.users.infrastructure.User;
import com.gscorp.dv1.users.infrastructure.UserRepository;

import lombok.RequiredArgsConstructor;

@Component("auditorProvider")
@RequiredArgsConstructor
public class SpringSecurityAuditorAware<CustomUserDetails> implements AuditorAware<User>{

    private final UserRepository userRepository;

    private static final Logger log = LoggerFactory.getLogger(SpringSecurityAuditorAware.class);

    @Override
    public Optional<User> getCurrentAuditor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
        log.debug("No authentication or not authenticated - returning empty auditor");
        return Optional.empty();
        }

        Object principal = auth.getPrincipal();
        log.debug("AuditorAware principal type: {}", principal != null ? principal.getClass().getName() : "null");

        // Si tu principal es SecurityUser (lo devuelves en UserDetailsServiceImpl)
        if (principal instanceof SecurityUser) {
        SecurityUser su = (SecurityUser) principal;
        Long userId = su.getId();
        if (userId != null) {
            log.debug("Returning auditor by id (proxy): {}", userId);
            return Optional.of(userRepository.getReferenceById(userId)); // proxy, evita SELECT inmediato
        }
        // fallback: si SecurityUser expone la entidad User
        User u = su.getUser();
        if (u != null && u.getId() != null) {
            log.debug("Returning auditor by user.getId(): {}", u.getId());
            return Optional.of(userRepository.getReferenceById(u.getId()));
        }
        }

        // Fallback por username (hará SELECT)
        String username = auth.getName();
        log.debug("Falling back to findByUsername for auditor: {}", username);
        return userRepository.findByUsername(username);
    }



}
