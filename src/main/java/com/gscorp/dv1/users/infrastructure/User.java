package com.gscorp.dv1.users.infrastructure;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.gscorp.dv1.clients.infrastructure.Client;
import com.gscorp.dv1.employees.infrastructure.Employee;
import com.gscorp.dv1.roles.infrastructure.Role;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
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

    @Column(name="username", unique=true, nullable=false, length=64)
    private String username;

    private String mail;

    private String phone;

    private String password;

    private Boolean active;

    private String invitationToken;

    private LocalDateTime invitationTokenExpiry;

    @ManyToMany (fetch = FetchType.EAGER) //Carga los roles junto con el usuario.
    @JoinTable (
        name = "users_role",
        joinColumns = @JoinColumn(name="user_id"),
        inverseJoinColumns = @JoinColumn(name="role_id")
    )
    private Set<Role> roles = new HashSet<>();

    @ManyToMany (fetch = FetchType.LAZY) //Carga los clientes a los que el usuario tiene acceso.
    @JoinTable (
        name = "users_clients",
        joinColumns = @JoinColumn(name="user_id"),
        inverseJoinColumns = @JoinColumn(name="client_id")
    )

    @JsonIgnore
    private Set<Client> clients = new HashSet<>();

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    @JsonIgnore
    private Employee employee;

    // campo opcional para zona preferida del usuario (ej: "Europe/Madrid")
    @Column(name = "time_zone", length = 64)
    private String timeZone;

    // Fechas de auditoría (requieren dependencias Hibernate)
    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;

}
