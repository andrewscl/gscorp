package com.gscorp.dv1.auth.web;

import com.gscorp.dv1.auth.web.dto.ResendInvitationRequest;

import java.time.Duration;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.gscorp.dv1.auth.application.PasswordResetTokenService;
import com.gscorp.dv1.auth.application.UserInvitationService;
import com.gscorp.dv1.auth.infrastructure.PasswordResetToken;
import com.gscorp.dv1.auth.web.dto.UserInvitationEmailDto;
import com.gscorp.dv1.enums.TokenPurpose;
import com.gscorp.dv1.enums.UserStatus;
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
    private final UserInvitationService userInvitationService;

    @Autowired
    private TemplateEngine templateEngine;

    private static final Duration INVITE_TTL = Duration.ofDays(7);

    @PostMapping("/invite")
    public ResponseEntity<?> inviteUser(@RequestBody InviteUserRequest request) {

        try {
            UserInvitationEmailDto inviteData = userInvitationService.prepareInvitation(request);

            String subject = "Bienvenido a SESAN";
            // Preparar las variables del proceso
            Context context = new Context();
            context.setVariable("username", inviteData.username());
            context.setVariable("token", inviteData.token());
            context.setVariable("name", inviteData.displayName());

            String cssPath = "static/css/email-user-invite.css";
            String htmlBody =
                templateEngine.process("auth/fragments/email-invitation", context);

            String finalHtmlBody =
                EmailTemplateUtils.buildStyledEmail(htmlBody, cssPath);

            gmailService.sendMail(inviteData.email(), subject, finalHtmlBody);

            return ResponseEntity.ok("Invitación enviada correctamente");

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("No se pudo enviar la invitación: " + e.getMessage());
        }
    }


    @PostMapping("/{externalId}/resend-invite")
    @Transactional
    public ResponseEntity<?> resendInvitationUser
                    (@PathVariable UUID externalId) {

        User user = userService.findByExternalId(externalId)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        user.setStatus(UserStatus.INVITED);
        userService.save(user);

        passwordResetTokenService.deleteByUserId(user.getId());

        PasswordResetToken token = passwordResetTokenService.createToken(user, INVITE_TTL, TokenPurpose.INVITATION);

        try {
            // Preparar las variables del proceso
            Context context = new Context();
            context.setVariable("username", user.getUsername());
            context.setVariable("token", token.getToken());
            context.setVariable("name", user.getEmployee() != null
                                            ? user.getEmployee().getName()
                                            : user.getUsername());

            String cssPath = "static/css/email-user-invite.css";
            String htmlBody =
                templateEngine.process("auth/fragments/email-invitation", context);

            String finalHtmlBody =
                EmailTemplateUtils.buildStyledEmail(htmlBody, cssPath);

            gmailService.sendMail(user.getMail(), "Bienvenido a SESAN", finalHtmlBody);

            return ResponseEntity.ok("Invitación enviada correctamente");

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("No se pudo enviar la invitación: " + e.getMessage());
        }
    }


    @PostMapping("/resend-expired-invites")
    public ResponseEntity<?> resendMassiveExpiredInvitations() {
        String message = userInvitationService.resendMassiveExpiredInvitations();
        return ResponseEntity.ok(message);
    }

    @PostMapping("/resend-employee-invite")
    public ResponseEntity<String> resendInvitationByEmployeeId(
        @RequestBody ResendInvitationRequest request) {

        var result = userInvitationService.prepareResendInvitationByEmployeeId(request.employeeExternalId());
        if (result.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        try {
            userInvitationService.executeResendInvitation(result.get());
            return ResponseEntity.ok("Invitación reenviada correctamente");
        } catch (IllegalStateException e) {
            log.error("Error al reenviar la invitación: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body("No se pudo reenviar la invitación: " + e.getMessage());
        }

    }



}