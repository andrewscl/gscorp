package com.gscorp.dv1.clients.infrastructure;

import java.util.HashSet;
import java.util.Set;

import com.gscorp.dv1.users.infrastructure.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.ManyToMany;
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

  @Builder.Default
  @Column(nullable=false)
  private Boolean active = true;   // ✅ se respeta en el builder

  // Relación inversa del ManyToMany
  @Builder.Default
  @ManyToMany(mappedBy = "clients", fetch = FetchType.LAZY)
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private Set<User> users = new HashSet<>();

  
}
