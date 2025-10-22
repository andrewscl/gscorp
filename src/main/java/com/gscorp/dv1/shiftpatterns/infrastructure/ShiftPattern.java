package com.gscorp.dv1.shiftpatterns.infrastructure;

import java.time.LocalDateTime;

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

    // Día de inicio del ciclo (ejemplo)
    private Integer startDay;

    // Fechas de auditoría (requieren dependencias Hibernate)
    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
