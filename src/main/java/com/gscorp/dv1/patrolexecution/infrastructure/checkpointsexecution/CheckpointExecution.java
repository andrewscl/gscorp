package com.gscorp.dv1.patrolexecution.infrastructure.checkpointsexecution;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.gscorp.dv1.patrol.infrastructure.checkpoints.PatrolCheckpoint;
import com.gscorp.dv1.patrolexecution.infrastructure.patrolsexecution.PatrolExecution;

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
@Table(name = "checkpoint_executions", indexes = {
    @Index(name = "ix_checkpoint_exec_execution_id", columnList = "patrol_execution_id")
})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class CheckpointExecution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_id", unique = true, nullable = false, updatable = false)
    private UUID externalId;

    @Column(nullable = false)
    private OffsetDateTime arrivalTime;

    @Column(name = "stay_time")
    private Integer stayTime;

    // Ubicación real capturada por el GPS del dispositivo al marcar
    @Column(name = "real_latitude", precision = 10, scale = 8)
    private BigDecimal realLatitude;

    @Column(name = "real_longitude", precision = 11, scale = 8)
    private BigDecimal realLongitude;

    @Column(length = 255)
    private String notes; // Por si el guardia reporta algo en el punto

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patrol_execution_id", nullable = false)
    private PatrolExecution patrolExecution;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "checkpoint_id", nullable = false)
    private PatrolCheckpoint checkpoint;

    @PrePersist
    protected void onCreate() {
        if (this.externalId == null) {
            this.externalId = UUID.randomUUID();
        }
    }

}
