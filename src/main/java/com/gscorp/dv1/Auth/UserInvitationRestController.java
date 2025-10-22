package com.gscorp.dv1.auth;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gscorp.dv1.services.GmailService;
import com.gscorp.dv1.users.application.UserService;
import com.gscorp.dv1.users.infrastructure.User;
import com.gscorp.dv1.users.web.dto.InviteUserRequest;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserInvitationRestController {

    private final UserService userService;
    private final GmailService gmailService;

    @PostMapping("/invite")
    public ResponseEntity<?> inviteUser(@RequestBody InviteUserRequest request) {
        // Crear el usuario invitado
        User user = userService.createInvitedUser(request);

        // Generar token y expiración
        String token = UUID.randomUUID().toString();
        user.setInvitationToken(token);
        user.setInvitationTokenExpiry(LocalDateTime.now().plusHours(24));
        user.setPassword(null);
        userService.save(user);

        // Enviar el correo de invitación con diseño mejorado y logo
        String subject = "Bienvenido a GSCorp";
        String htmlBody = """
            <div style="background:#f7f7f9;padding:32px;">
            <div style="max-width:420px;margin:auto;background:white;padding:32px 24px;border-radius:10px;box-shadow:0 2px 12px rgba(30,40,60,0.13);font-family:'Segoe UI','Roboto',Arial,sans-serif;">
                <div style="text-align:center;margin-bottom:24px;">
                <img src="https://gscorp.cl/img/logo-mail.png" alt="GSCorp Logo" style="height:56px;max-width:170px;">
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
            """.formatted(user.getUsername(), token, token, token);

        try {
            gmailService.sendMail(user.getMail(), subject, htmlBody);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("No se pudo enviar el correo de invitación: " + e.getMessage());
        }

        return ResponseEntity.ok("Invitación enviada correctamente");
    }
}