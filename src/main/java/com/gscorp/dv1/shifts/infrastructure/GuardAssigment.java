package com.gscorp.dv1.shifts.infrastructure;

import com.gscorp.dv1.guards.infrastructure.Guard;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity @Table(name="guard_assignments",
  uniqueConstraints = @UniqueConstraint(name="uq_assignment_shift_guard", columnNames={"shift_id","guard_id"}),
  indexes = @Index(name="ix_assign_shift", columnList="shift_id")
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class GuardAssigment {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) Long id;

    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    @JoinColumn(name="shift_id", nullable=false)
    Shift shift;

    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    @JoinColumn(name="guard_id", nullable=false)
    Guard guard;
    
}
