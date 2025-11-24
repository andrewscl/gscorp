package com.gscorp.dv1.config;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.gscorp.dv1.users.application.UserService;


import lombok.RequiredArgsConstructor;

@Component("auditorProvider")
@RequiredArgsConstructor
public class SpringSecurityAuditorAware implements AuditorAware<Long> {

    private static final Logger log = LoggerFactory.getLogger(SpringSecurityAuditorAware.class);

    private final UserService userService;

    @Override
    public Optional<Long> getCurrentAuditor() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                log.debug("No auth -> empty auditor");
                return Optional.empty();
            }
            Long userId = userService.getUserIdFromAuthentication(auth);
            log.debug("AuditorAware resolved userId={}", userId);
            return Optional.ofNullable(userId);
        } catch (Exception e) {
            log.warn("Error resolving auditor id", e);
            return Optional.empty();
        }
    }

}