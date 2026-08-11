package com.gscorp.dv1.operations.shifts.infrastructure;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.gscorp.dv1.enums.ShiftStatus;
import com.gscorp.dv1.operations.shiftassignments.infrastructure.ShiftAssignment;
import com.gscorp.dv1.operations.shiftrequests.infrastructure.ShiftRequest;
import com.gscorp.dv1.operations.sites.infrastructure.Site;

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

@Entity 
@Table(name="shifts",
  indexes = {
    @Index(name="ix_shifts_site", columnList="site_id"),
    @Index(name="ix_shifts_range", columnList="start_ts,end_ts"),
    @Index(name="ix_shifts_request_range", columnList="shift_request_id, start_ts")
  }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Shift {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Builder.Default
    @Column(name = "external_id", unique=true,
                        nullable=false, updatable=false)
    private UUID externalId = UUID.randomUUID();

    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    @JoinColumn(name="site_id", nullable=false)
    private Site site;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shift_assignment_id")
    private ShiftAssignment assignment;

    @Column(name="shift_date", nullable=false)
    private LocalDate shiftDate;

    @Column(name="start_ts", nullable=false)
    private OffsetDateTime startTs;

    @Column(name="end_ts",   nullable=false)
    private OffsetDateTime endTs;

    @Column(name="description", length=500)
    private String description;

    @Column(name="lunch_time")
    private Integer lunchTime;

    @Enumerated(EnumType.STRING)
    @Column(name="shift_status")
    private ShiftStatus status;

    @ManyToOne(optional=true, fetch=FetchType.LAZY)
    @JoinColumn(name="shift_request_id")
    private ShiftRequest shiftRequest;

    @Column(nullable = false)
    private String createdBy;

    @Column(nullable = true)
    private String updatedBy;

    @CreationTimestamp
    @Column(name = "created_at", columnDefinition = "TIMESTAMP WITH TIME ZONE", updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime updatedAt;

}
