package com.gscorp.dv1.patrol.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity @Table(name="patrol_checkpoints",
  uniqueConstraints = @UniqueConstraint(name="uq_cp_route_order", columnNames={"route_id","order_n"}),
  indexes = @Index(name="ix_cp_route", columnList="route_id")
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PatrolCheckPoint {

  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) Long id;

  @ManyToOne(optional=false, fetch=FetchType.LAZY)
  @JoinColumn(name="route_id", nullable=false)
  PatrolRoute route;

  @Column(nullable=false, length=160) String name;
  Double lat; Double lon;

  @Column(name="order_n", nullable=false) Integer orderN;
  /** Tolerancia de distancia (m) para considerar “dentro” del checkpoint. */

  @Builder.Default
  @Column(nullable=false)
  private Integer toleranceM = 30;

}
