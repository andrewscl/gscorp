package com.gscorp.dv1.auth.application;

import com.gscorp.dv1.auth.web.dto.UserInvitationEmailDto;
import com.gscorp.dv1.users.web.dto.InviteUserRequest;

import java.util.Optional;
import java.util.UUID;

public interface UserInvitationService {

    UserInvitationEmailDto prepareInvitation (InviteUserRequest request);

    String resendMassiveExpiredInvitations();

    Optional<UserInvitationEmailDto> prepareResendInvitationByEmployeeId(UUID employeeExternalId);

    void executeResendInvitation (UserInvitationEmailDto dto);

}
