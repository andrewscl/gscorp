package com.gscorp.dv1.attendance.infrastructure;

import java.time.OffsetDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

  @Column(name = "user_id", nullable=false)
  Long userId;

  @Column(name="ts", nullable=false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
  OffsetDateTime ts;

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

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "site_id")
  private Site site;

  // Nueva relación hacia User (lectura, userId sigue siendo la columna "oficial")
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", insertable = false, updatable = false)
  @JsonIgnore
  private User user;

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
