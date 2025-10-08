package com.gscorp.dv1.patrol.infrastructure;

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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity @Table(name="patrol_routes",
  indexes = @Index(name="ix_route_site", columnList="site_id")
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PatrolRoute {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) Long id;

    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    @JoinColumn(name="site_id", nullable=false)
    Site site;

    @Column(nullable=false, length=160) String name;
    /** Cantidad esperada por turno (para compliance %). */

    @Builder.Default
    @Column(nullable=false)
    private Integer expectedPerShift = 1;
    
}
