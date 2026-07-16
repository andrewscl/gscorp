package com.gscorp.dv1.hr.hrdocuments.infrastructure;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table (name="employee_documents")
@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
@Builder
public class HrDocumentType {

    @Id @GeneratedValue
    private Long id;

    @Builder.Default
    @Column(name = "external_id", unique=true,
                        nullable=false, updatable=false)
    private UUID externalId = UUID.randomUUID();

    @Column(nullable = false, unique = true, length = 50)
    private String code; // Ej: "NOTICE_LETTER", "EQUIPMENT_RECEIPT", "SEVERANCE_DEED"

    @Column(nullable = false, length = 100)
    private String name; // Ej: "Carta de Aviso de Término", "Acta de Devolución de Equipos"

    @Builder.Default
    @Column(nullable = false)
    private Boolean required = true;

    @Column(length = 20)
    private String targetStatus; // El estado al que pertenece (ej: "GIVEN_NOTICE", "INACTIVE", "SETTLED")

    @Column(nullable = true, updatable = false)
    private String createdBy;

    @Column(nullable = true)
    private String updatedBy;

    @CreationTimestamp
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (this.externalId == null) {
            this.externalId = UUID.randomUUID();
        }
    }
}
