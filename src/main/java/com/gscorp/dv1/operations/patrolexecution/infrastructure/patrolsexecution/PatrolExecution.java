package com.gscorp.dv1.operations.patrolexecution.infrastructure.patrolsexecution;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;

import com.gscorp.dv1.enums.PatrolExecutionStatus;
import com.gscorp.dv1.operations.patrol.infrastructure.patrols.Patrol;
import com.gscorp.dv1.operations.patrol.infrastructure.schedules.PatrolSchedule;
import com.gscorp.dv1.operations.patrolexecution.infrastructure.checkpointsexecution.CheckpointExecution;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "patrol_executions", indexes = {
    @Index(name = "ix_patrol_exec_external_id", columnList = "external_id"),
    @Index(name = "ix_patrol_exec_patrol_id", columnList = "patrol_id"),
    @Index(name = "ix_patrol_exec_schedule_id", columnList = "patrol_schedule_id")
})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class PatrolExecution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_id", unique = true,
                        nullable = false, updatable = false)
    private UUID externalId;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant startTime;

    private Instant endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PatrolExecutionStatus status ;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patrol_id", nullable = false)
    private Patrol patrol;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patrol_schedule_id", nullable = true) // nullable = true por si en el futuro permites rondas "libres" fuera de agenda
    private PatrolSchedule patrolSchedule;

    // Vinculación al guardia (asumiendo que manejas Username o ID de usuario)
    @Column(nullable = false)
    private Long userId;

    // Vinculación al guardia (asumiendo que manejas Username o ID de usuario)
    @Column(nullable = false)
    private Long employeeId;

    @Column(name = "description", length=255)
    private String description;

    @Column(length=255)
    private String photoPath;

    @Column(length=255)
    private String videoPath;

    @Column(name="latitude")
    private BigDecimal latitude;

    @Column(name="longitude")
    private BigDecimal longitude;

    @CreatedBy
    @Column(nullable = true)
    private String createdBy;

    @LastModifiedBy
    @Column(nullable = true)
    private String updatedBy;

    // Guarda la zona IANA utilizada para interpretar/mostrar la hora (ej. "America/Santiago")
    @Column(name = "client_timezone", length = 64)
    private String clientTimezone;

    // Fuente de la zona (REQUESTED | USER_PROFILE | SYSTEM_DEFAULT)
    @Column(name = "timezone_source", length = 32)
    private String timezoneSource;

    @OneToMany(mappedBy = "patrolExecution", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CheckpointExecution> checkpointExecutions = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (this.externalId == null) this.externalId = UUID.randomUUID();
    }

    public void addCheckpointExecution(CheckpointExecution execution) {
        checkpointExecutions.add(execution);
        execution.setPatrolExecution(this);
    }

}
