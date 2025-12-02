package com.gscorp.dv1.auth;

import java.time.Duration;

import org.springframework.http.ResponseEntity;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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

    private static final Duration INVITE_TTL = Duration.ofHours(24);

    @PostMapping("/invite")
    public ResponseEntity<?> inviteUser(@RequestBody InviteUserRequest request) {
        // Crear el usuario invitado
        User user = userService.createInvitedUser(request);

        // Crear el token de invitación (válido por 24 horas)
        PasswordResetToken token = passwordResetTokenService.
                                            createToken(user, INVITE_TTL);

        // Enviar el correo de invitación con diseño mejorado y logo
        String subject = "Bienvenido a GSCorp";
        String htmlBody = """
            <div style="background:#f7f7f9;padding:32px;">
            <div style="max-width:420px;margin:auto;background:white;padding:32px 24px;border-radius:10px;box-shadow:0 2px 12px rgba(30,40,60,0.13);font-family:'Segoe UI','Roboto',Arial,sans-serif;">
                <div style="text-align:center;margin-bottom:24px;">
                <img src="https://gscorp.cl/img/logo-mail.png" alt="GSCorp Logo" style="height:90px;max-width:260px;">
                </div>
                <h2 style="color:#2d3a4b;margin-bottom:16px;text-align:center;">¡Bienvenido/a a la plataforma!</h2>
                <p style="font-size:1.06rem;color:#444;">
                Hola <b>%s</b>,
                </p>
                <p style="font-size:1.06rem;color:#444;">
                Te han invitado a la plataforma. Haz click en el siguiente botón para definir tu contraseña:
                </p>
                <div style="text-align:center;margin:32px 0 28px 0;">
                <a href="https://gscorp.cl/auth/define-password?token=%s"
                    style="background:#4f8cff;color:white;padding:14px 28px;border-radius:6px;text-decoration:none;font-weight:600;font-size:1.12rem;display:inline-block;">
                    Definir contraseña
                </a>
                </div>
                <p style="font-size:0.99rem;color:#777;">
                Si el botón no funciona, copia y pega este enlace en tu navegador:<br>
                <a href="https://gscorp.cl/auth/define-password?token=%s" style="color:#4f8cff;">
                    https://gscorp.cl/auth/define-password?token=%s
                </a>
                </p>
                <p style="color:#a0a3ad;font-size:0.93rem;margin-top:20px;">
                Este enlace expira en 24 horas.
                </p>
            </div>
            </div>
            """.formatted(user.getUsername(), token.getToken(), token.getToken(), token.getToken());

        try {
            gmailService.sendMail(user.getMail(), subject, htmlBody);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("No se pudo enviar el correo de invitación: " + e.getMessage());
        }

        return ResponseEntity.ok("Invitación enviada correctamente");
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
        return "https://gscorp.cl/auth/define-password?token=" + token;
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