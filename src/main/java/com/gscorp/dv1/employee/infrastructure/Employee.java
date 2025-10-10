package com.gscorp.dv1.employee.infrastructure;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

    @Entity
    @Table (name="employee")
    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public class Employee {
        
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        private String noame;
        private String mail;
        private String password;

        
}