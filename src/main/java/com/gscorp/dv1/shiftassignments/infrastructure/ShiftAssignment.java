package com.gscorp.dv1.shiftassignments.infrastructure;

import java.time.OffsetDateTime;

import com.gscorp.dv1.employees.infrastructure.Employee;
import com.gscorp.dv1.enums.ShiftAssignmentStatus;
import com.gscorp.dv1.shifts.infrastructure.Shift;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="shift_assignments")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ShiftAssignment {

    @Id @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="shift_id", nullable=false)
    private Shift shift;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="employee_id", nullable=false)
    private Employee employee;

    // Opcional: código del empleado, si lo necesitas rápido
    @Column(name="employee_code", length=32)
    private String employeeCode;

    @Enumerated(EnumType.STRING)
    @Column(name="shift_assignment_status", length = 20)
    private ShiftAssignmentStatus status;

    private String note;

    private OffsetDateTime assignedAt;

}
