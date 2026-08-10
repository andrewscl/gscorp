package com.gscorp.dv1.operations.shiftpatterns.infrastructure;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.LongStream;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table (name="shift_pattern")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ShiftPattern {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_id", unique=true,
                            nullable=true, updatable=false)
    private UUID externalId;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private Long workDays;

    @Column(nullable = false)
    private Long restDays;

    // Identificador corto para el patrón, ej: "4x2"
    @Column(unique = true, length = 16)
    private String code;

    // Estado activo/inactivo
    @Builder.Default
    private Boolean active = true;

    // Fechas de auditoría (requieren dependencias Hibernate)
    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public List<Long> getCycleDaysList() {
        if (this.workDays == null || this.restDays == null){
            return List.of();
        }
        long totalDays = this.workDays + this.restDays;
        return LongStream
                    .rangeClosed(1, totalDays)
                        .boxed().toList();
    }

    public Long getTotalDays() {
        if (this.workDays == null || this.restDays == null) {
            return 0L;
        }
        return this.workDays + this.restDays;
    }

}




