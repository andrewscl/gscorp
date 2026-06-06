package com.gscorp.dv1.users.infrastructure;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.gscorp.dv1.clients.infrastructure.Client;
import com.gscorp.dv1.companies.infrastructure.Company;
import com.gscorp.dv1.employees.infrastructure.Employee;
import com.gscorp.dv1.enums.UserStatus;
import com.gscorp.dv1.roles.infrastructure.Role;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table (name="dbuser")
@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_id", unique=true,
                        nullable=false, updatable=false)
    private UUID externalId;

    @Column(name="username", unique=true, nullable=false, length=64)
    private String username;

    private String mail;

    private String phone;

    private String password;

    private Boolean active;

    @Enumerated(EnumType.STRING)
    @Column(name="status")
    private UserStatus status;

    @ManyToOne (fetch = FetchType.EAGER)
    @JoinColumn (name = "role_id")
    private Role role;

    @ManyToMany (fetch = FetchType.LAZY)
    @JoinTable (
        name = "users_companies",
        joinColumns = @JoinColumn(name="user_id"),
        inverseJoinColumns = @JoinColumn(name="company_id")
    )
    @JsonIgnore
    private Set<Company> companies = new HashSet<>();

    @ManyToMany (fetch = FetchType.LAZY)
    @JoinTable (
        name = "users_clients",
        joinColumns = @JoinColumn(name="user_id"),
        inverseJoinColumns = @JoinColumn(name="client_id")
    )
    @JsonIgnore
    private Set<Client> clients = new HashSet<>();

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "user")
    @JsonIgnore
    private Employee employee;

    @Column(name = "time_zone", length = 64)
    private String timeZone;

    @CreationTimestamp
    private OffsetDateTime createdAt;
    @UpdateTimestamp
    private OffsetDateTime updatedAt;

    @CreatedBy
    @Column(nullable = false)
    private String createdBy;

    @LastModifiedBy
    @Column(nullable = true)
    private String updatedBy;

    @PrePersist
    protected void onCreate() {
        if (this.externalId == null) {
            this.externalId = UUID.randomUUID();
        }
    }

}
