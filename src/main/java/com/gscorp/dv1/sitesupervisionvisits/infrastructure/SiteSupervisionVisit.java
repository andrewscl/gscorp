package com.gscorp.dv1.sitesupervisionvisits.infrastructure;

import java.time.OffsetDateTime;

import com.gscorp.dv1.employees.infrastructure.Employee;
import com.gscorp.dv1.sites.infrastructure.Site;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Index;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "site_supervision_visits",
  indexes = {
    @Index(name="ix_visit_site", columnList="site_id"),
    @Index(name="ix_visit_supervisor", columnList="supervisor_id")
  }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SiteSupervisionVisit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    @JoinColumn(name="supervisor_id", nullable=false)
    private Employee supervisor;

    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    @JoinColumn(name="site_id", nullable=false)
    private Site site;

    @Column(nullable=false)
    private OffsetDateTime visitDateTime;

    @Column(length=500)
    private String description;

    @Column(length=255)
    private String photoPath;

    @Column(length=255)
    private String videoPath;

}