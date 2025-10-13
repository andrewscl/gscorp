package com.gscorp.dv1.projects.infrastructure;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import com.gscorp.dv1.clients.infrastructure.Client;
import com.gscorp.dv1.employees.infrastructure.Employee;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "project")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String code;          // Código interno del proyecto
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean active = true;

    // Relación: Un proyecto pertenece a un único cliente
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    private Client client;

    // Relación: Un proyecto tiene varios empleados, y un empleado puede estar en varios proyectos
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "employee_project",
        joinColumns = @JoinColumn(name = "project_id"),
        inverseJoinColumns = @JoinColumn(name = "employee_id")
    )
    private Set<Employee> employees = new HashSet<>();
}