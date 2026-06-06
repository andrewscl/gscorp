package com.gscorp.dv1.config;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component("auditorProvider")
@RequiredArgsConstructor
public class SpringSecurityAuditorAware implements AuditorAware<String> {

    private static final Logger log = LoggerFactory.getLogger(SpringSecurityAuditorAware.class);

    @Override
    public Optional<String> getCurrentAuditor() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
                log.debug("No auth -> empty auditor");
                return Optional.of("SYSTEM");
            }

            String username = auth.getName();
            log.debug("AuditorAware resolved username={}", username);
            return Optional.ofNullable(username);

        } catch (Exception e) {
            log.warn("Error resolving auditor username", e);
            return Optional.of("SYSTEM");
        }
    }

}