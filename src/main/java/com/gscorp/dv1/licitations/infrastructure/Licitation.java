package com.gscorp.dv1.licitations.infrastructure;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import com.gscorp.dv1.licitations.web.dto.AwardedLicitationDto;
import com.gscorp.dv1.licitations.web.dto.ItemLicitationDto;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "mp_licitations")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Licitation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String externalCode; // Código único de la licitación
    private String name;
    
    @Column(length = 2000)
    private String description;

    private String status; // Estado de la licitación
    private String type;   // Tipo de licitación

    private String buyerName;    // Nombre del organismo comprador
    private String buyerRut;     // Rut del organismo comprador

    @Temporal(TemporalType.TIMESTAMP)
    private Date publishDate;

    @Temporal(TemporalType.TIMESTAMP)
    private Date closeDate;

    @Temporal(TemporalType.TIMESTAMP)
    private Date openDate;

    @Temporal(TemporalType.TIMESTAMP)
    private Date awardDate;

    private Double estimatedAmount;
    private String currency;

    private String category;   // Categoría principal de la licitación
    private String sector;     // Rubro principal (sector)
    private String subCategory;

    private String region;
    private String commune;

    private String contactName;
    private String contactEmail;
    private String contactPhone;

    private String basesUrl;
    private String recordUrl;

    private Instant lastSync; // Fecha/hora de última sincronización

    @Column(length = 5000)
    private String jsonData; // JSON completo de la licitación (para flexibilidad)

    // Relación con ítems de la licitación
    @OneToMany(mappedBy = "licitation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ItemLicitationDto> items;

    // Relación con adjudicados (proveedores adjudicados)
    @OneToMany(mappedBy = "licitation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AwardedLicitationDto> awarded;    

}
