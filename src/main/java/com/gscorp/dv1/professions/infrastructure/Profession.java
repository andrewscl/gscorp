package com.gscorp.dv1.professions.infrastructure;

import java.util.HashSet;
import java.util.Set;

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

    @ManyToMany(mappedBy = "professions")
    @Builder.Default
    private Set<Employee> employees = new HashSet<>();
}
