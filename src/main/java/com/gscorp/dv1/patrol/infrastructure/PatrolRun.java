package com.gscorp.dv1.patrol.infrastructure;

import java.time.OffsetDateTime;

import com.gscorp.dv1.shifts.infrastructure.Shift;

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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@Entity @Table(name="patrol_runs",
  indexes = {
    @Index(name="ix_run_route", columnList="route_id"),
    @Index(name="ix_run_shift", columnList="shift_id"),
    @Index(name="ix_run_status", columnList="status")
  }
)
public class PatrolRun {

  public enum Status { PLANNED, IN_PROGRESS, DONE, CANCELED, INCOMPLETE }

  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional=false, fetch=FetchType.LAZY)
  @JoinColumn(name="route_id", nullable=false)
  @lombok.NonNull
  private PatrolRoute route;

  @ManyToOne(optional=false, fetch=FetchType.LAZY)
  @JoinColumn(name="shift_id", nullable=false)
  @lombok.NonNull
  private Shift shift;

  private OffsetDateTime startedTs;
  private OffsetDateTime endedTs;

  @Builder.Default
  @Enumerated(EnumType.STRING)
  @Column(nullable=false)
  private Status status = Status.PLANNED;

  @Builder.Default
  @Column(nullable=false, columnDefinition="integer not null default 0")
  private Integer expectedHits = 0;

  @Builder.Default
  @Column(nullable=false, columnDefinition="integer not null default 0")
  private Integer completedHits = 0;

  @PrePersist
  void prePersist() {
    if (status == null) status = Status.PLANNED;
    if (expectedHits == null) expectedHits = 0;
    if (completedHits == null) completedHits = 0;
  }

}
