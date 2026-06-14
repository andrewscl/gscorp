package com.gscorp.dv1.auth.application;

import com.gscorp.dv1.auth.web.dto.UserInvitationEmailDto;
import com.gscorp.dv1.users.web.dto.InviteUserRequest;

public interface UserInvitationService {

    UserInvitationEmailDto prepareInvitation (InviteUserRequest request);

}
