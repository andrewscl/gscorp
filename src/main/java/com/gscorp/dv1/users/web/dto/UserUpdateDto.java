package com.gscorp.dv1.users.web.dto;

import java.util.Set;

/**
 * DTO para actualizar un User. Campos no nulos se aplican (merge parcial).
 * Ajusta los campos según necesidades de negocio.
 */
public record UserUpdateDto(
    String username,
    String mail,
    Boolean active,
    Long roleId,
    Set<Long> companyIds,
    Set<Long> clientIds,
    Long employeeId,
    String timeZone
) {
    
}