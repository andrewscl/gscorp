package com.gscorp.dv1.incidents.infrastructure;

import java.time.OffsetDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.gscorp.dv1.enums.IncidentType;
import com.gscorp.dv1.enums.Priority;
import com.gscorp.dv1.sites.infrastructure.Site;
import com.gscorp.dv1.users.infrastructure.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
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

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name="incidents",
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

    @Enumerated(EnumType.STRING)
    @Column(name="type", nullable=false)
    private IncidentType incidentType;

    @Enumerated(EnumType.STRING)
    @Column(name="priority", nullable=false)
    private Priority priority;

    @Builder.Default
    @Enumerated(EnumType.STRING) @Column(nullable=false)
    private Status status = Status.OPEN;

    @CreationTimestamp
    @Column(name="opened_ts", nullable=false, updatable=false)
    OffsetDateTime openedTs;

    @Column(name="first_response_ts")
    OffsetDateTime firstResponseTs;

    @Column(name="closed_ts")
    OffsetDateTime closedTs;

    /** SLA objetivo (min) efectivo del incidente (puede venir de type.defaultSlaMinutes). */
    @Builder.Default
    @Column(nullable=false)
    private Integer slaMinutes = 30;

    @Column(columnDefinition="text") String description;

    @Column(length=255)
    private String photoPath;

    @Column(name= "created_at", nullable=false, updatable=false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = OffsetDateTime.now();
    }

    // sólo el extracto modificado/añadido
    @CreatedBy
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by_id", nullable = false)
    private User createdBy;

}
