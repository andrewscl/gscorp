package com.gscorp.dv1.clients.web.dto;

//DTO Salida
public record ClientDto (Long id, String name, String taxId, String contactEmail, Boolean active){}
