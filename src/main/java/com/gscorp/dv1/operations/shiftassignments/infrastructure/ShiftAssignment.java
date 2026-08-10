package com.gscorp.dv1.operations.shiftassignments.infrastructure;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.gscorp.dv1.enums.ShiftAssignmentStatus;
import com.gscorp.dv1.hr.employees.infrastructure.Employee;
import com.gscorp.dv1.operations.shiftrequests.infrastructure.ShiftRequest;
import com.gscorp.dv1.operations.shifts.infrastructure.Shift;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
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

        @Builder.Default
        @Column(name = "external_id", unique=true,
                            nullable=false, updatable=false)
        private UUID externalId = UUID.randomUUID();

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name="employee_id", nullable=false)
        private Employee employee;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "shift_request_id", nullable = false)
        private ShiftRequest shiftRequest;

        @Enumerated(EnumType.STRING)
        @Column(name="shift_assignment_status", length = 20)
        private ShiftAssignmentStatus status;

        @OneToMany(mappedBy = "assignment", cascade = CascadeType.ALL)
        @Builder.Default
        private List<Shift> shifts = new ArrayList<>();

        private String notes;

        private OffsetDateTime assignedAt;
        private OffsetDateTime assignedUntil;

        @Column(name="start_cycle_number")
        private Integer startCycleNumber;

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
