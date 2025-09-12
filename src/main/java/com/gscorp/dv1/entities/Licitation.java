package com.gscorp.dv1.entities;

import java.time.Instant;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name="mp_licitations")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Licitation {
    
    @Id
    @Column(name="codigo_externo", length=40, nullable=false,updatable=false)
    private String externalCode;

    @Column(length=500)
    private String name;

    @Column(length=50)
    private String status;

    private LocalDate publishDate;
    private LocalDate closeDate;

    @Column(length=200)
    private String buyerName;

    @Column(length=50)
    private String buyerCode;

    @Column(nullable=false)
    private Instant lastSync = Instant.now();
}
