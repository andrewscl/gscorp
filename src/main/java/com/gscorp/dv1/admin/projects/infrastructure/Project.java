package com.gscorp.dv1.admin.projects.infrastructure;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.gscorp.dv1.admin.clients.infrastructure.Client;
import com.gscorp.dv1.enums.ProjectStatus;
import com.gscorp.dv1.hr.employees.infrastructure.Employee;
import com.gscorp.dv1.operations.sites.infrastructure.Site;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "project")
@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
@Builder
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_id", unique=true,
                        nullable=true, updatable=false)
    private UUID externalId;

    private String name;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name="project_status", length = 20)
    private ProjectStatus status;

    @Builder.Default
    private Boolean active = true;

    // Relación: Un proyecto pertenece a un único cliente
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    @JsonIgnore
    private Client client;

    @Builder.Default
    @OneToMany(mappedBy = "project", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<Site> sites = new HashSet<>();

    // Relación inversa del ManyToMany definido en User.roles
    @Builder.Default
    @ManyToMany(mappedBy = "projects", fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Employee> employees = new HashSet<>();

    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(nullable = true, updatable = false)
    private String createdBy;

    @Column(nullable = true)
    private String updatedBy;

    @PrePersist
    protected void onCreate() {
        if (this.externalId == null) {
            this.externalId = UUID.randomUUID();
        }
    }

}