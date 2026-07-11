package com.gscorp.dv1.admin.clients.infrastructure;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.gscorp.dv1.admin.companies.infrastructure.Company;
import com.gscorp.dv1.enums.ClientStatus;
import com.gscorp.dv1.users.infrastructure.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@Entity @Table(name="clients",
  indexes = { @Index(name="ix_clients_name", columnList="name") }
)
public class Client {

  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "external_id", unique=true,
                        nullable=true, updatable=false)
  private UUID externalId;

  @Column(nullable=false, length=160)
  private String name;

  @Column(nullable=false, length=200)
  private String legalName;

  @Column(length=32)
  private String taxId;

  @Column(length=100)
  private String contactEmail;

  @Column(length=30)
  private String contactPhone;

  @Enumerated(EnumType.STRING)
  @Column(name = "client_status", nullable = false)
  private ClientStatus status;

  @Builder.Default
  @Column(nullable=false)
  private Boolean active = true;   // ✅ se respeta en el builder

  // Relación inversa del ManyToMany
  @Builder.Default
  @ManyToMany(mappedBy = "clients", fetch = FetchType.LAZY)
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private Set<User> users = new HashSet<>();

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "company_id", nullable=true)
  private Company company;

  @CreationTimestamp
  private OffsetDateTime createdAt;

  @UpdateTimestamp
  private OffsetDateTime updatedAt;

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
