package com.gscorp.dv1.patrol.infrastructure;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity @Table(name="patrol_checkpoints")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PatrolCheckpoint {

  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long id;

  @Column(nullable=false, length=160)
  String name;

  Double lat;
  Double lon;

  // Relación con SiteCheckpoint
  @Builder.Default
  @OneToMany(mappedBy = "checkpoint", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<SiteCheckpoint> siteCheckpoints = new ArrayList<>();

  @Column(name="order_n", nullable=false) Integer orderN;
  /** Tolerancia de distancia (m) para considerar “dentro” del checkpoint. */

  @Builder.Default
  @Column(nullable=false)
  private Integer toleranceM = 30;

}
