package com.gscorp.dv1.issues.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity @Table(name="incident_types",
  uniqueConstraints = @UniqueConstraint(name="uq_inc_types_code", columnNames="code")
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class IssueType {

  public enum Severity { LOW, MEDIUM, HIGH }

  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) Long id;


  @Column(nullable=false, length=40)  String code;  // ej: "INTRUSION"
  @Column(nullable=false, length=160) String name;  // ej: "Intrusión"
  
  @Builder.Default
  @Enumerated(EnumType.STRING) @Column(nullable=false)
  private Severity severity = Severity.LOW;

  /** SLA sugerido (minutos) para primera respuesta/cierre. */
  @Builder.Default
  @Column(nullable=false)
  private Integer defaultSlaMinutes = 30;
    
}
