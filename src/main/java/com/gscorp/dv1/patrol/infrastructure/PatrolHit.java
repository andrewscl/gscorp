package com.gscorp.dv1.patrol.infrastructure;

import java.time.OffsetDateTime;

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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity @Table(name="patrol_hits",
  indexes = {
    @Index(name="ix_hits_run", columnList="run_id"),
    @Index(name="ix_hits_checkpoint", columnList="checkpoint_id"),
    @Index(name="ix_hits_ts", columnList="ts")
  }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PatrolHit {
    
 @Id @GeneratedValue(strategy = GenerationType.IDENTITY) Long id;

  @ManyToOne(optional=false, fetch=FetchType.LAZY)
  @JoinColumn(name="run_id", nullable=false)
  PatrolRun run;

  @ManyToOne(optional=false, fetch=FetchType.LAZY)
  @JoinColumn(name="checkpoint_id", nullable=false)
  PatrolCheckPoint checkpoint;

  @Column(nullable=false) OffsetDateTime ts;
  Double lat; Double lon;
  Boolean withinGeo;     // calculado según toleranceM
  Double distanceM;      // opcional: distancia al punto objetivo

}
