package com.gscorp.dv1.tasks.infrastructure;

import java.time.LocalDate;

import com.gscorp.dv1.users.infrastructure.User;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "task")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Task {

    @Id @GeneratedValue
    private Long id;
    private String title;
    private String description;
    private String status; // PENDIENTE, EN_PROGRESO, FINALIZADA, CANCELADA
    private String priority; // BAJA, MEDIA, ALTA
    private LocalDate dueDate;

    @ManyToOne
    private User assignedTo;

    @ManyToOne
    private User createdBy;
    private LocalDate createdAt;

}