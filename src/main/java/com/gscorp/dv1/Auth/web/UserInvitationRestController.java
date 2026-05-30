package com.gscorp.dv1.auth.web;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.springframework.util.StringUtils;

import com.gscorp.dv1.auth.application.PasswordResetTokenService;
import com.gscorp.dv1.auth.infrastructure.PasswordResetToken;
import com.gscorp.dv1.services.GmailService;
import com.gscorp.dv1.services.WhatsAppService;
import com.gscorp.dv1.users.application.UserService;
import com.gscorp.dv1.users.infrastructure.User;
import com.gscorp.dv1.users.web.dto.InviteUserRequest;
import com.gscorp.dv1.users.web.dto.InviteUserRequestWhatsApp;
import com.gscorp.dv1.users.web.dto.InviteUserResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserInvitationRestController {

    private final UserService userService;
    private final GmailService gmailService;
    private final PasswordResetTokenService passwordResetTokenService;
    private final WhatsAppService whatsAppService;

    @Autowired
    private TemplateEngine templateEngine;

    private static final Duration INVITE_TTL = Duration.ofHours(24);

    @PostMapping("/invite")
    public ResponseEntity<?> inviteUser(@RequestBody InviteUserRequest request) {
        // Crear el usuario invitado
        User user = userService.createInvitedUser(request);
        // Crear el token de invitación (válido por 24 horas)
        PasswordResetToken token = passwordResetTokenService.
                                            createToken(user, INVITE_TTL);

        String subject = "Bienvenido a SESAN";

        try {
            // Preparar las variables del proceso
            Context context = new Context();
            context.setVariable("username", user.getUsername());
            context.setVariable("token", token.getToken());

            // Renderizar la plantilla externa
            String htmlBody =
                templateEngine.process("auth/fragments/email-invitation", context);

            // Enviar 
            gmailService.sendMail(user.getMail(), subject, htmlBody);

            return ResponseEntity.ok("Invitación enviada correctamente");

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("No se pudo enviar la invitación" + e.getMessage());
        }
    }


    @PostMapping("/invite-whatsapp")
    public ResponseEntity<?> inviteUserViaWhatsApp(@RequestBody InviteUserRequestWhatsApp request) {
        // Crear el usuario invitado
        User user = userService.createInvitedUserWhatsApp(request);

        // Crear el token de invitación (válido por 24 horas)
        PasswordResetToken token = passwordResetTokenService.
                                            createToken(user, INVITE_TTL);

        // Build plain text message and magic link (WhatsApp should receive plain text with link)
        String link = buildDefinePasswordLink(token.getToken());
        String plainText = String.format("Hola %s, activa tu acceso aquí (expira en 24h): %s",
                user.getUsername(), link);

        Runnable sendWhatsApp = () -> {
            try {
                if (!StringUtils.hasText(user.getPhone())) {
                    log.warn("User {} has no phone; skipping WhatsApp send", user.getId());
                    return;
                }
                // Use sendMagicLink (or sendTextMessage) — services should be async
                whatsAppService.sendTextMessage(user.getPhone(), plainText);
                log.info("WhatsApp invite sent (queued) to userId={} phone={}", user.getId(), user.getPhone());
            } catch (Exception e) {
                log.error("Failed to send WhatsApp invite to userId={} phone={}: {}", user.getId(), user.getPhone(), e.getMessage(), e);
                // persist send-failure for retries if needed
            }
        };

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    sendWhatsApp.run();
                }
            });
        } else {
            sendWhatsApp.run();
        }

        InviteUserResponse resp = new InviteUserResponse(
                                                user.getId(),
                                                token.getId(),
                                                maskPhone(user.getPhone()),
                                                "Invitación por WhatsApp programada",
                                                "sent");
        return ResponseEntity.ok(resp);

    }

    // ---------- helpers ----------

    private String buildDefinePasswordLink(String token) {
        // prefer configurable base url in properties
        return "https://sesan.cl/auth/define-password?token=" + token;
    }

    private String maskPhone(String phone) {
        if (!StringUtils.hasText(phone)) return null;
        String s = phone.trim();
        if (s.length() <= 6) return s;
        int keep = 3;
        String last = s.substring(s.length() - keep);
        return s.substring(0, Math.min(4, s.length() - keep)) + "****" + last;
    }


}