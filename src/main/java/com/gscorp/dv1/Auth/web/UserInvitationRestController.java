package com.gscorp.dv1.auth.web;

import java.time.Duration;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.gscorp.dv1.auth.application.PasswordResetTokenService;
import com.gscorp.dv1.auth.infrastructure.PasswordResetToken;
import com.gscorp.dv1.employees.application.EmployeeService;
import com.gscorp.dv1.employees.web.dto.EmployeeSelectDto;
import com.gscorp.dv1.exceptions.ResourceNotFoundException;
import com.gscorp.dv1.services.EmailTemplateUtils;
import com.gscorp.dv1.services.GmailService;
import com.gscorp.dv1.users.application.UserService;
import com.gscorp.dv1.users.infrastructure.User;
import com.gscorp.dv1.users.web.dto.InviteUserRequest;

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
    private final EmployeeService employeeService;

    @Autowired
    private TemplateEngine templateEngine;

    private static final Duration INVITE_TTL = Duration.ofHours(24);

    @PostMapping("/invite")
    public ResponseEntity<?> inviteUser(@RequestBody InviteUserRequest request) {
        // Crear el usuario invitado
        User user = userService.createInvitedUser(request);
        // Crear el token de invitación (válido por 24 horas)
        EmployeeSelectDto employee = employeeService.findEmployeeSelectDtoById(request.employeeId());

        PasswordResetToken token = passwordResetTokenService.
                                            createToken(user, INVITE_TTL);

        String subject = "Bienvenido a SESAN";

        try {
            // Preparar las variables del proceso
            Context context = new Context();
            context.setVariable("username", user.getUsername());
            context.setVariable("token", token.getToken());
            context.setVariable("name", employee.name());

            String cssPath = "static/css/email-user-invite.css";
            String htmlBody =
                templateEngine.process("auth/fragments/email-invitation", context);

            String finalHtmlBody =
                EmailTemplateUtils.buildStyledEmail(htmlBody, cssPath);

            gmailService.sendMail(user.getMail(), subject, finalHtmlBody);

            return ResponseEntity.ok("Invitación enviada correctamente");

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("No se pudo enviar la invitación: " + e.getMessage());
        }
    }


    @PostMapping("/{externalId}/resend-invite")
    public ResponseEntity<?> resendInvitationUser
                    (@PathVariable UUID externalId) {

        User user = userService.findByExternalId(externalId)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        EmployeeSelectDto employee =
                    employeeService.findEmployeeByUserId(user.getId());

        passwordResetTokenService.deleteByUserId(user.getId());

        PasswordResetToken token = passwordResetTokenService.createToken(user, INVITE_TTL);

        String subject = "Bienvenido a SESAN";

        try {
            // Preparar las variables del proceso
            Context context = new Context();
            context.setVariable("username", user.getUsername());
            context.setVariable("token", token.getToken());
            context.setVariable("name", employee.name());

            String cssPath = "static/css/email-user-invite.css";
            String htmlBody =
                templateEngine.process("auth/fragments/email-invitation", context);

            String finalHtmlBody =
                EmailTemplateUtils.buildStyledEmail(htmlBody, cssPath);

            gmailService.sendMail(user.getMail(), subject, finalHtmlBody);

            return ResponseEntity.ok("Invitación enviada correctamente");

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("No se pudo enviar la invitación: " + e.getMessage());
        }

    }

}