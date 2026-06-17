package com.gscorp.dv1.hr.web.dto;

public record CompanyUserStatDto (
    String companyName,
    long totalEmployees,
    long activeUsers,
    long blockedUsers,
    long withoutUser
){
    
}
