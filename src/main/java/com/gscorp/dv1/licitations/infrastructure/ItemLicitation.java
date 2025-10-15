package com.gscorp.dv1.licitations.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
public class ItemLicitation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String code;
    private String name;

    @Column(length = 2000)
    private String description;

    private Double quantity;
    private String unit;
    private Double estimatedAmount;
    private String currency;
    private String category;
    private String subCategory;

    // Relación con la licitación padre
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "licitation_id")
    private Licitation licitation;

}
