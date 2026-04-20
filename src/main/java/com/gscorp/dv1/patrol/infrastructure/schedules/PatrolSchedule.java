package com.gscorp.dv1.patrol.infrastructure.schedules;

import java.time.LocalTime;
import java.util.UUID;

import com.gscorp.dv1.patrol.infrastructure.patrols.Patrol;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "patrol_schedules",
        indexes = {
            @Index(name = "ix_patrol_schedules_patrol_id", columnList = "patrol_id"),
            @Index(name = "ix_patrol_schedules_external_id", columnList = "external_id")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatrolSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //Indicador para uso API Frontend
    @Column(name = "external_id", unique = true, nullable = false, updatable = false)
    private UUID externalId;

    @Column(nullable=false)
    private LocalTime startTime;

    @Builder.Default
    @Column(nullable=false)
    private Boolean active = true;

    // Relación de pertenencia al Patrol
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patrol_id", nullable = false)
    private Patrol patrol;

    @PrePersist
    protected void onCreate() {
        if (externalId == null) {
            externalId = UUID.randomUUID();
        }
    }

}
