package com.gscorp.dv1.publicmarket.infrastructure;

import java.time.OffsetDateTime;
import java.util.List;

import com.gscorp.dv1.enums.LicitationCurrency;
import com.gscorp.dv1.enums.LicitationStatus;
import com.gscorp.dv1.enums.LicitationType;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "mp_licitations")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class PublicMarketLicitation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, unique = true, length = 100)
    private String externalCode;

    @Column(nullable=false, length=255)
    private String name;
    
    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private LicitationStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private LicitationType type;

    @Column(length=255)
    private String buyerName;

    @Column(length=50)
    private String buyerRut;

    @Column(nullable = false)
    private OffsetDateTime publishDate;

    @Column(nullable = false)
    private OffsetDateTime closeDate;

    private OffsetDateTime openDate;
    private OffsetDateTime awardDate;

    private Double estimatedAmount;

    @Enumerated(EnumType.STRING)
    @Column(length=10)
    private LicitationCurrency currency;

    @Column(length=255)
    private String category;

    @Column(length=255)
    private String sector;

    @Column(length=255)
    private String subCategory;

    @Column(length=100)
    private String region;

    @Column(length=100)
    private String commune;

    @Column(length=255)
    private String contactName;

    @Column(length=255)
    private String contactEmail;

    @Column(length=50)
    private String contactPhone;

    @Column(length=1000)
    private String basesUrl;
    @Column(length=1000)
    private String recordUrl;

    private OffsetDateTime lastSync; // Fecha/hora de última sincronización

    @Column(length = 5000)
    private String jsonData; // JSON completo de la licitación (para flexibilidad)

    // Relación con ítems de la licitación
    @OneToMany(mappedBy = "licitation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PMItemLicitation> items;

}
