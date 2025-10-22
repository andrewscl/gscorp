package com.gscorp.dv1.professions.infrastructure;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.gscorp.dv1.employees.infrastructure.Employee;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table (name="profession")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Profession {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    // descripción de la profesión
    @Column(length = 300)
    private String description;

    // código interno único
    @Column(unique = true, length = 50)
    private String code;

    // estado activo/inactivo
    @Builder.Default
    private Boolean active = true;

    // categoría o agrupación
    private String category;

    // nivel de profesión
    private Integer level;

    @ManyToMany(mappedBy = "professions")
    @Builder.Default
    private Set<Employee> employees = new HashSet<>();

    // Fechas de auditoría (requieren dependencias Hibernate)
    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;

}
