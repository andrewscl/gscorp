package com.gscorp.dv1.attendance.infrastructure;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import com.gscorp.dv1.sites.infrastructure.Site;

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

  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id;
  @Column(nullable=false) Long userId;

  @Column(nullable=false)
  OffsetDateTime ts;

  Double lat;
  Double lon;
  Double accuracyM;
  
  @Column(nullable=false, length=8)
  String action;      // "in" | "out"

  @Column(nullable=false) Boolean locationOk;
  Double distanceM;

  @Column(columnDefinition="text")
  String deviceInfo;

  @JdbcTypeCode(SqlTypes.INET)
  @Column(columnDefinition="inet")
  String ip;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "site_id")
  private Site site;

  @PrePersist void onCreate(){ if (ts == null) ts = OffsetDateTime.now(); }

    // Fechas de auditoría (requieren dependencias Hibernate)
    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;

  
  
}
