package com.gscorp.dv1.licitations.infrastructure;

import java.util.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "mp_awarded_licitations")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AwardedLicitation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String supplierName;
    private String supplierRut;
    private Double amount;
    private String currency;

    @Temporal(TemporalType.TIMESTAMP)
    private Date awardDate;

    // Relación con la licitación padre
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "licitation_id")
    private Licitation licitation;
}
