package com.gscorp.dv1.auth.application;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.gscorp.dv1.auth.infrastructure.PasswordResetToken;
import com.gscorp.dv1.enums.TokenPurpose;
import com.gscorp.dv1.auth.web.dto.UserInvitationEmailDto;
import com.gscorp.dv1.enums.AccountType;
import com.gscorp.dv1.enums.UserStatus;
import com.gscorp.dv1.hr.employees.application.EmployeeService;
import com.gscorp.dv1.hr.employees.web.dto.EmployeeSelectDto;
import com.gscorp.dv1.services.EmailTemplateUtils;
import com.gscorp.dv1.services.GmailService;
import com.gscorp.dv1.users.application.UserService;
import com.gscorp.dv1.users.infrastructure.User;
import com.gscorp.dv1.users.infrastructure.UserRepository;
import com.gscorp.dv1.users.web.dto.InviteUserRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserInvitationServiceImpl implements UserInvitationService {

    private final UserService userService;
    private final PasswordResetTokenService passwordResetTokenService;
    private final EmployeeService employeeService;
    private final UserRepository userRepo;
    private final GmailService gmailService;
    private final TemplateEngine templateEngine; 
    
    private static final Duration INVITE_TTL = Duration.ofHours(24);

    @Override
    @Transactional
    public UserInvitationEmailDto prepareInvitation (InviteUserRequest request) {

        if(userRepo.existsByUsername(request.username())) {
            throw new IllegalArgumentException("El nombre de usuario ya está registrado en el sistema.");
        }
        User user = userService.createInvitedUser(request);
        PasswordResetToken token = passwordResetTokenService.
                                            createToken(user, INVITE_TTL, TokenPurpose.INVITATION);
        String displayName = user.getUsername();
        if (user.getRole().getAccountType() == AccountType.COMPANY && request.employeeId() != null) {
            EmployeeSelectDto employee = employeeService.findEmployeeSelectDtoById(request.employeeId());
            if (employee != null) {
                displayName = employee.name();
            }
        }
        return new UserInvitationEmailDto(
            user.getMail(),
            user.getUsername(),
            displayName,
            token.getToken()
        ); 
    }


    @Transactional
    public String resendMassiveExpiredInvitations() {
        List<User> expiredInvitedUsers = userRepo.findByStatus(UserStatus.EXPIRED);
        if(expiredInvitedUsers.isEmpty()) {
            return "No hay usuarios con invitaciones expiradas para reenviar.";
        }
        int successCount = 0;
        for(User user : expiredInvitedUsers) {
            try {
                user.setStatus(UserStatus.INVITED);
                userRepo.save(user);
                EmployeeSelectDto employee =
                    employeeService.findEmployeeByUserId(user.getId());
                passwordResetTokenService.deleteByUserId(user.getId());
                PasswordResetToken token = passwordResetTokenService.createToken(user, INVITE_TTL, TokenPurpose.INVITATION);

                String subject = "Bienvenido a SESAN";
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
                successCount++;
            } catch (Exception e) {
                log.error("Error al reenviar invitación para el usuario con ID {}: {}", user.getId(), e.getMessage());
            }
        }
        return String.format("Se reenviaron %d invitaciones de un total de %d usuarios con invitaciones expiradas."
                                                                                , successCount, expiredInvitedUsers.size());
    }


    @Override
    @Transactional
    public Optional<UserInvitationEmailDto> prepareResendInvitationByEmployeeId(UUID employeeExternalId) {
        if (employeeExternalId == null) {
            throw new IllegalArgumentException("El ID externo del empleado no puede ser nulo.");
        }
        User user = userRepo.findByEmployeeExternalId(employeeExternalId)
            .orElseThrow(
                () -> new IllegalArgumentException("No se encontró un usuario para" + 
                                    "el empleado con ID externo: " + employeeExternalId));

        PasswordResetToken token = passwordResetTokenService.createToken(user, INVITE_TTL, TokenPurpose.INVITATION);

        String displayName = user.getEmployee() != null 
                                ? user.getEmployee().getName()
                                : user.getUsername();

        return Optional.of(new UserInvitationEmailDto(
                user.getMail(),
                user.getUsername(),
                displayName,
                token.getToken()
        ));
    }

    @Override
    public void executeResendInvitation (UserInvitationEmailDto dto) {
        if (dto == null || dto.email() == null || dto.email().isBlank() ||
                            dto.username() == null || dto.username().isBlank()) {
            throw new IllegalArgumentException("Username or email cannot be null or empty for invitation resend");
        }
        Context context = new Context();
        context.setVariable("username", dto.username());
        context.setVariable("token", dto.token());
        context.setVariable("name", dto.displayName());
        String cssPath = "static/css/email-resend-invitation.css";
        String htmlBody =
                templateEngine.process("auth/fragments/email-invitation", context);
        String finalHtmlBody =
                EmailTemplateUtils.buildStyledEmail(htmlBody, cssPath);
        try {
            gmailService.sendMail(dto.email(), "Reenvío de invitación", finalHtmlBody);
        } catch (Exception e) {
            log.error("Failed to send invitation email to {}", dto.email(), e);
            throw new IllegalStateException("Failed to send invitation email", e);
        }

    }

}
