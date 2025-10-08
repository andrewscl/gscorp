package com.gscorp.dv1.clients.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

  @Column(length=32)
  private String taxId;

  @Builder.Default
  @Column(nullable=false)
  private Boolean active = true;   // ✅ se respeta en el builder
}
