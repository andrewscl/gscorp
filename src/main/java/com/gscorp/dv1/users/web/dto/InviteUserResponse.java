package com.gscorp.dv1.users.web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO simplificado con Lombok.
 * No incluye el token raw por seguridad.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InviteUserResponse {
    private Long userId;
    private Long tokenId;
    private String phone;   // opcional: enmascarar antes de setear
    private String message; // texto informativo opcional
    private String status;  // opcional: "scheduled", "sent", "failed", etc.

}
