package com.gscorp.dv1.users.web.dto;

import java.util.Set;

public record InviteUserRequestWhatsApp (
    String username,
    String phone,
    Set<Long> roleIds,
    Set<Long> clientIds,
    Long employeeId
){

}
