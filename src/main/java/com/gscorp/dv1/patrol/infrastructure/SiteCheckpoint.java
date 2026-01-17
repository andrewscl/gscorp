package com.gscorp.dv1.patrol.infrastructure;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

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
import jakarta.persistence.UniqueConstraint;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "site_checkpoints",
       uniqueConstraints = @UniqueConstraint(name = "uq_site_checkpoint", columnNames = {"site_id", "checkpoint_id"}),
       indexes = {
           @Index(name = "ix_site_checkpoint_site", columnList = "site_id"),
           @Index(name = "ix_site_checkpoint_checkpoint", columnList = "checkpoint_id")
       })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SiteCheckpoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relación con Site
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", nullable = false)
    private Site site;

    // Relación con PatrolCheckPoint
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "checkpoint_id", nullable = false)
    private PatrolCheckpoint checkpoint;

    // Campos adicionales
    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;

    @Column(nullable = false)
    @CreationTimestamp
    private LocalDateTime assignedAt;

    private String notes; // Comentarios sobre la asignación
    
}
