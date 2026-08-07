package com.gscorp.dv1.operations.shiftassignments.infrastructure;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.gscorp.dv1.enums.ShiftAssignmentStatus;
import com.gscorp.dv1.hr.employees.infrastructure.Employee;
import com.gscorp.dv1.operations.shiftpatterns.infrastructure.ShiftPattern;
import com.gscorp.dv1.operations.shifts.infrastructure.Shift;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

    @Entity
    @Table(name="shift_assignments")
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public class ShiftAssignment {

        @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(name = "external_id", unique=true,
                            nullable=false, updatable=false)
        private UUID externalId;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name="shift_id", nullable=false)
        private Shift shift;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name="employee_id", nullable=false)
        private Employee employee;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "shift_pattern_id", nullable = false)
        private ShiftPattern shiftPattern;

        @Enumerated(EnumType.STRING)
        @Column(name="shift_assignment_status", length = 20)
        private ShiftAssignmentStatus status;

        private String notes;

        private OffsetDateTime assignedAt;

        // Día de inicio del ciclo
        private Integer startDay;

        @Column(nullable = true, updatable = false)
        private String createdBy;

        @Column(nullable = true)
        private String updatedBy;

        @CreationTimestamp
        private OffsetDateTime createdAt;

        @UpdateTimestamp
        private OffsetDateTime updatedAt;

        @PrePersist
        protected void onCreate() {
            if (this.externalId == null) {
                this.externalId = UUID.randomUUID();
            }
        }
    }
