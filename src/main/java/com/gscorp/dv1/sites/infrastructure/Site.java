package com.gscorp.dv1.sites.infrastructure;

import com.gscorp.dv1.projects.infrastructure.Project;

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

@Entity
@Table(name = "sites",
  indexes = {
    @Index(name="ix_sites_project", columnList="project_id"),
    @Index(name="ix_sites_name", columnList="name")
  }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Site {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    @JoinColumn(name="project_id")
    Project project;

    @Column(nullable=false, length=160)
    String name;

    @Column(length=240)
    String address;

    Double lat;
    Double lon;

    @Column(length=64)
    String timeZone; // "America/Santiago"

    @Builder.Default
    @Column(nullable=false)
    private Boolean active = true;
}