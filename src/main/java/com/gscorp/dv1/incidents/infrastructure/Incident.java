package com.gscorp.dv1.incidents.infrastructure;

import java.time.OffsetDateTime;

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

@Entity @Table(name="incidents",
  indexes = {
    @Index(name="ix_inc_site", columnList="site_id"),
    @Index(name="ix_inc_status", columnList="status"),
    @Index(name="ix_inc_opened", columnList="opened_ts")
  }
)
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public class Incident {

    public enum Status { OPEN, IN_PROGRESS, CLOSED }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) Long id;

    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    @JoinColumn(name="site_id", nullable=false)
    Site site;

    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    @JoinColumn(name="type_id", nullable=false)
    IncidentType type;

    @Builder.Default
    @Enumerated(EnumType.STRING) @Column(nullable=false)
    private Status status = Status.OPEN;

    @Column(name="opened_ts", nullable=false) OffsetDateTime openedTs;
    @Column(name="first_response_ts")         OffsetDateTime firstResponseTs;
    @Column(name="closed_ts")                 OffsetDateTime closedTs;

    /** SLA objetivo (min) efectivo del incidente (puede venir de type.defaultSlaMinutes). */
    @Builder.Default
    @Column(nullable=false)
    private Integer slaMinutes = 30;

    @Column(columnDefinition="text") String description;

}
