package com.gscorp.dv1.patrol.infrastructure;

import java.time.OffsetDateTime;
import java.time.OffsetTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

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
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table (name="patrols", indexes = {
    @Index(name = "ix_patrols_name", columnList = "name"),
    @Index(name = "ix_patrols_site_id", columnList = "site_id")
    })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Patrol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, length=64)
    private String name;

    @Column(nullable=false, length=160)
    private String description;

    @Column(name = "day_from", nullable = false, length = 1)
    private Integer dayFrom;

    @Column(name = "day_to", nullable = false, length = 1)
    private Integer dayTo;

    @Column(nullable=false)
    private OffsetTime startTime;

    @Column(nullable=true)
    private OffsetTime endTime;

    @Builder.Default
    @Column(nullable=false)
    private Boolean active = true;

    @CreationTimestamp
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    private OffsetDateTime updatedAt;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", nullable = false)
    private Site site;

    @Column(nullable = false)
    private String createdBy;

    @Column(nullable = true)
    private String updatedBy;

    @PrePersist
    private void onPrePersist() {
        if(startTime != null && endTime != null && startTime.isAfter(endTime)) {
            throw new IllegalArgumentException("startTime no puede ser posterior a endTime");
        }
    }

    @PreUpdate
    private void onPreUpdate() {
        if(startTime != null && endTime != null && startTime.isAfter(endTime)) {
            throw new IllegalArgumentException("startTime no puede ser posterior a endTime");
        }
    }

}
