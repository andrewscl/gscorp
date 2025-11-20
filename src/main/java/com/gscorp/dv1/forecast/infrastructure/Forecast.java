package com.gscorp.dv1.forecast.infrastructure;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.gscorp.dv1.clients.infrastructure.Client;
import com.gscorp.dv1.enums.ForecastCategory;
import com.gscorp.dv1.enums.Periodicity;
import com.gscorp.dv1.enums.Units;
import com.gscorp.dv1.projects.infrastructure.Project;
import com.gscorp.dv1.sites.infrastructure.Site;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
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
import jakarta.persistence.Version;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


/**
 * Híbrido: ids almacenados + relaciones lazy (read-only) para JOIN FETCH cuando haga falta.
 * Requiere que existan las entidades Client, Project, Site en esos paquetes.
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "forecasts",
       indexes = {
         @Index(name = "ix_forecast_client_period", columnList = "client_id,period_start"),
         @Index(name = "ix_forecast_site_period", columnList = "site_id,period_start")
       })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Forecast {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "client_id", nullable = false)
    private Long clientId;

    @Column(name = "project_id")
    private Long projectId;

    @Column(name = "site_id")
    private Long siteId;

    // Relaciones para lectura (no insert/update desde JPA)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", insertable = false, updatable = false)
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", insertable = false, updatable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", insertable = false, updatable = false)
    private Site site;

    @Enumerated(EnumType.STRING)
    @Column(name="forecast_category", nullable=false)
    private ForecastCategory forecastCategory;

    @Enumerated(EnumType.STRING)
    @Column(name="periodicity", nullable=false)
    private Periodicity periodicity;

    @NotNull
    @Column(nullable = false, length = 80)
    private String metric;

    @NotNull
    @Column(name = "period_start", nullable = false)
    private OffsetDateTime periodStart;

    @NotNull
    @Column(name = "period_end", nullable = false)
    private OffsetDateTime periodEnd;

    @Column(name = "period_start_hour")
    private Integer periodStartHour;

    @Column(name = "period_end_hour")
    private Integer periodEndHour;

    @NotNull
    @DecimalMin(value = "0", message = "value must be >= 0")
    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal value;

    @Enumerated(EnumType.STRING)
    @Column(name="units", nullable=false)
    private Units units;

    @Column(length = 64)
    private String tz;

    @Column(columnDefinition = "text")
    private String note;

    @Column(precision = 5, scale = 2)
    private BigDecimal confidence;

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

    @Version
    private Long rowVersion;
}