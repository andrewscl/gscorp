package com.gscorp.dv1.auth.application;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import com.gscorp.dv1.enums.TokenPurpose;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.gscorp.dv1.auth.infrastructure.PasswordResetToken;
import com.gscorp.dv1.auth.web.dto.PasswordRecoveryDto;
import com.gscorp.dv1.enums.UserStatus;
import com.gscorp.dv1.services.EmailTemplateUtils;
import com.gscorp.dv1.services.GmailService;
import com.gscorp.dv1.users.infrastructure.User;
import com.gscorp.dv1.users.infrastructure.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j 
@Service
@RequiredArgsConstructor 
public class PasswordRecoveryServiceImpl implements PasswordRecoveryService {

    private final UserRepository userRepository;
    private final PasswordResetTokenService passwordResetTokenService;
    private final GmailService gmailService;

    @Autowired
    private TemplateEngine templateEngine;

    private static final Duration RECOVERY_TTL = Duration.ofMinutes(30);

    @Override 
    @Transactional
    public Optional<PasswordRecoveryDto> prepareRecovery(String usernameOrEmail) {
        if (usernameOrEmail == null || usernameOrEmail.isBlank()) {
            throw new IllegalArgumentException("Username or email cannot be null or empty");
        }
        List<User> matches = userRepository.findByUsernameOrEmail(usernameOrEmail.trim(), UserStatus.ACTIVE);
        if (matches.size() != 1) {
            return Optional.empty();
        }
        User user = matches.get(0);
        PasswordResetToken token = passwordResetTokenService.createToken(
                                                    user, RECOVERY_TTL, TokenPurpose.PASSWORD_RECOVERY);
        String displayName = user.getEmployee() != null 
                                ? user.getEmployee().getName()
                                : user.getUsername();
        return Optional.of(new PasswordRecoveryDto(
                user.getMail(),
                user.getUsername(),
                displayName,
                token.getToken()
        ));
    }

    @Override
    public void executeRecovery(PasswordRecoveryDto dto) {
        if (dto == null || dto.email() == null || dto.email().isBlank() ||
                            dto.username() == null || dto.username().isBlank()) {
            throw new IllegalArgumentException("Username or email cannot be null or empty");
        }
        Context context = new Context();
        context.setVariable("username", dto.username());
        context.setVariable("token", dto.token());
        context.setVariable("name", dto.displayName());
        String cssPath = "static/css/email-recovery.css";
        String htmlBody =
                templateEngine.process("auth/fragments/email-recovery", context);
        String finalHtmlBody =
                EmailTemplateUtils.buildStyledEmail(htmlBody, cssPath);
        try {
            gmailService.sendMail(dto.email(), "Restablece tu contraseña", finalHtmlBody);
        } catch (Exception e) {
            log.error("Failed to send recovery email to {}", dto.email(), e);
            throw new IllegalStateException("Failed to send recovery email", e);
        }
    }

}
