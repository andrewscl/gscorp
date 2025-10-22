package com.gscorp.dv1.professions.web.dto;

public record CreateProfessionRequest (
    String name,
    String description,
    String code,
    Boolean active,
    String category,
    Integer level
) {
    
}
