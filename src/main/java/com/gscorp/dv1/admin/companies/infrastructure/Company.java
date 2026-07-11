package com.gscorp.dv1.admin.companies.infrastructure;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.gscorp.dv1.admin.clients.infrastructure.Client;
import com.gscorp.dv1.enums.CompanyStatus;
import com.gscorp.dv1.hr.employees.infrastructure.Employee;
import com.gscorp.dv1.users.infrastructure.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table (name="companies", indexes = {
    @Index(name = "ix_companies_name", columnList = "name")
    })
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_id", unique=true,
                        nullable=false, updatable=false)
    private UUID externalId;

    @Column(nullable=false, length=160)
    private String name;

    @Column(nullable=false, length=200)
    private String legalName;

    @Column(length=32)
    private String taxId;

    @Column(name = "company_status", nullable = false)
    private CompanyStatus status;

    @OneToMany(mappedBy = "company", fetch = FetchType.LAZY)
    private List<Client> clients;

    @OneToMany(mappedBy = "company", fetch = FetchType.LAZY)
    private List<Employee> employees;

    // Relación inversa del ManyToMany
    @Builder.Default
    @ManyToMany(mappedBy = "companies", fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<User> users = new HashSet<>();

    @Builder.Default
    @Column(nullable=false)
    private Boolean active = true;

    @CreationTimestamp
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    private OffsetDateTime updatedAt;

    @Column(nullable = false, updatable = false)
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