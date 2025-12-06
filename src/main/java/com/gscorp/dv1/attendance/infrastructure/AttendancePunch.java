package com.gscorp.dv1.attendance.infrastructure;

import java.time.OffsetDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import com.gscorp.dv1.employees.infrastructure.Employee;
import com.gscorp.dv1.sites.infrastructure.Site;
import com.gscorp.dv1.users.infrastructure.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity @Table(name="attendance_punches")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AttendancePunch {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    Long id;

    // FK columns (escribibles directamente)
    @Column(name = "site_id")
    private Long siteId;

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @Column(name = "user_id")
    private Long userId;

    // Relaciones solo lectura (no insert/update desde JPA)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", insertable = false, updatable = false)
    private Site site;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="employee_id", insertable =false, updatable=false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @Column(name="ts", nullable=false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    OffsetDateTime ts;

    @Column(name="client_ts", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    OffsetDateTime clientTs;

    Double lat;
    Double lon;
    Double accuracyM;
  
    @Column(nullable=false, length=8)
    String action;      // "in" | "out"

    @Column(nullable=false)
    Boolean locationOk;

    Double distanceM;

    @Column(columnDefinition="text")
    String deviceInfo;

    @JdbcTypeCode(SqlTypes.INET)
    @Column(columnDefinition="inet")
    String ip;

    // Guarda la zona IANA utilizada para interpretar/mostrar la hora (ej. "America/Santiago")
    @Column(name = "client_timezone", length = 64)
    private String clientTimezone;

    // Fuente de la zona (REQUESTED | USER_PROFILE | SYSTEM_DEFAULT)
    @Column(name = "timezone_source", length = 32)
    private String timezoneSource;

    @PrePersist
    void onCreate() {
      if (ts == null) ts = OffsetDateTime.now();
    }

    @CreationTimestamp
    @Column(name = "created_at", columnDefinition = "TIMESTAMP WITH TIME ZONE", updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime updatedAt;

}
