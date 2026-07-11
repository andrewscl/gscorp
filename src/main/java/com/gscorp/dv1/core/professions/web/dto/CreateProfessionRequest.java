package com.gscorp.dv1.core.professions.web.dto;

public record CreateProfessionRequest (
    String name,
    String description,
    String code,
    Boolean active,
    String category,
    Integer level
) {
    
}
