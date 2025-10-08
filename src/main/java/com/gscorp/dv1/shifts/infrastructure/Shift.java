package com.gscorp.dv1.shifts.infrastructure;

import java.time.OffsetDateTime;

import com.gscorp.dv1.sites.infrastructure.Site;

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

@Entity @Table(name="shifts",
  indexes = {
    @Index(name="ix_shifts_site", columnList="site_id"),
    @Index(name="ix_shifts_range", columnList="start_ts,end_ts")
  }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Shift {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) Long id;

    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    @JoinColumn(name="site_id", nullable=false)
    Site site;

    @Column(name="start_ts", nullable=false) OffsetDateTime startTs;
    @Column(name="end_ts",   nullable=false) OffsetDateTime endTs;

    /** Guardia(s) planificados para KPI present/absent. */

    @Builder.Default
    @Column(nullable=false)
    private Integer plannedGuards = 1;

}
