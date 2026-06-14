package com.gscorp.dv1.auth.application;

import java.time.Duration;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gscorp.dv1.auth.infrastructure.PasswordResetToken;
import com.gscorp.dv1.auth.web.dto.UserInvitationEmailDto;
import com.gscorp.dv1.employees.application.EmployeeService;
import com.gscorp.dv1.employees.web.dto.EmployeeSelectDto;
import com.gscorp.dv1.enums.AccountType;
import com.gscorp.dv1.users.application.UserService;
import com.gscorp.dv1.users.infrastructure.User;
import com.gscorp.dv1.users.web.dto.InviteUserRequest;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserInvitationServiceImpl implements UserInvitationService {

    private final UserService userService;
    private final PasswordResetTokenService passwordResetTokenService;
    private final EmployeeService employeeService;
    
    private static final Duration INVITE_TTL = Duration.ofHours(24);

    @Override
    @Transactional
    public UserInvitationEmailDto prepareInvitation (InviteUserRequest request) {

        User user = userService.createInvitedUser(request);

        PasswordResetToken token = passwordResetTokenService.
                                            createToken(user, INVITE_TTL);

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

}
