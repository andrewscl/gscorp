package com.gscorp.dv1.shifts.infrastructure;

import java.time.OffsetDateTime;

import com.gscorp.dv1.enums.ShiftStatus;
import com.gscorp.dv1.enums.ShiftType;
import com.gscorp.dv1.shiftrequests.infrastructure.ShiftRequest;
import com.gscorp.dv1.sites.infrastructure.Site;

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

    @Column(name="code", length=50)
    String code;

    @Column(name="description", length=500)
    String description;

    @Enumerated(EnumType.STRING)
    @Column(name="shift_type")
    private ShiftType shiftType;

    @Column(name="week_days", length=64)
    String weekDays;

    @Column(name="lunch_time")
    Integer lunchTime; // en minutos

    @Enumerated(EnumType.STRING)
    @Column(name="shift_status")
    private ShiftStatus shiftStatus;

    /** Guardia(s) planificados para KPI present/absent. */

    @Builder.Default
    @Column(nullable=false)
    private Integer plannedGuards = 1;

    @ManyToOne(optional=true, fetch=FetchType.LAZY)
    @JoinColumn(name="shift_request_id")
    ShiftRequest shiftRequest;

}
