package com.gscorp.dv1.sitesupervisionvisits.infrastructure;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
    name = "site_supervision_visits_forecast",
    indexes = {
        @Index(name = "ix_visit_site", columnList = "site_id"),
        @Index(name = "ix_forecast_site_period", columnList = "site_id,period_start"),
        @Index(name = "ix_forecast_client_metric_period", columnList = "client_id,metric,period_start")
    },
    // opcional: habilita si quieres prohibir duplicados exactos por version de negocio
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_forecast_site_metric_period_version",
                          columnNames = {"site_id", "metric", "period_start", "periodicity", "forecast_version"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Forecast {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // opcional si aplicas forecasts por cliente
    @Column(name = "client_id")
    private Long clientId;

    @Column(name = "site_id")
    private Long siteId;

    // p.ej. "daily", "hourly" — considera Enum si quieres restricciones
    @Column(nullable = false, length = 64)
    private String periodicity;

    // métrica a la que aplica el forecast (p.ej. "visits")
    @Column(nullable = false, length = 80)
    private String metric;

    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;

    // Valor pronosticado
    @Column(nullable = false, precision = 12, scale = 3)
    @DecimalMin("0")
    private BigDecimal value;

    @Column(length = 32)
    private String units;

    @Column(length = 64)
    private String tz;

    @Column(columnDefinition = "text")
    private String note;

    // Confianza (0..100)
    @Column(precision = 5, scale = 2)
    private BigDecimal confidence;

    // Versión de negocio del forecast (diferente de @Version JPA)
    @Builder.Default
    @Column(name = "forecast_version")
    private Integer forecastVersion = 1;

    @CreatedBy
    @Column(name = "created_by")
    private Long createdBy;

    @LastModifiedBy
    @Column(name = "updated_by")
    private Long updatedBy;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    // Optimistic locking para evitar sobrescrituras concurrentes
    @Version
    private Long rowVersion;
}