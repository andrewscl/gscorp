package com.gscorp.dv1.publicmarket.infrastructure;

import com.gscorp.dv1.enums.LicitationCurrency;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "mp_licitation_items")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class PMItemLicitation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length=100)
    private String code;

    @Column(nullable = false, length=255)
    private String name;

    @Column(length = 2000)
    private String description;

    private Double quantity;

    @Column(length=50)
    private String unit;

    private Double estimatedAmount;

    @Enumerated(EnumType.STRING)
    private LicitationCurrency currency;
    
    private String category;
    private String subCategory;

    // Relación con la licitación padre
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "licitation_id")
    private PublicMarketLicitation licitation;

}
