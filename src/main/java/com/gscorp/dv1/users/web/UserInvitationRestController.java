package com.gscorp.dv1.users.web;

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
        //Crear el usuario invitado
        User user = userService.createInvitedUser(request);

        //Genera token y expiración
        String token = UUID.randomUUID().toString();
        user.setInvitationToken(token);
        user.setInvitationTokenExpiry(LocalDateTime.now().plusHours(24));
        user.setPassword(null);
        userService.save(user);

        //Enviar el correo de invitación
        String subject = "Bienvenido/a a la plataforma - Define tu contraseña";
        String htmlBody = """
            <p>Hola %s,</p>
            <p>Te han invitado a la plataforma. Haz click en el siguiente enlace para definir tu contraseña:</p>
            <p>
            <a href="https://tuapp.com/definir-contraseña?token=%s">Definir contraseña</a>
            </p>
            <p>Este enlace expira en 24 horas.</p>
        """.formatted(user.getUsername(), token);

        try {
            gmailService.sendMail(user.getMail(), subject, htmlBody);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("No se pudo enviar el correo de invitación: " + e.getMessage());
        }

        return ResponseEntity.ok("Invitación enviada correctamente");
        }
}
