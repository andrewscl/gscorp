package com.gscorp.dv1.hr.employeeterminations.infrastructure;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.gscorp.dv1.enums.EmployeeTransitionStatus;
import com.gscorp.dv1.enums.TerminationReason;
import com.gscorp.dv1.hr.employeedocs.infrastructure.HumanResourcesDocument;
import com.gscorp.dv1.hr.employees.infrastructure.Employee;

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
@Table (name="employee_terminations")
@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
@Builder
public class EmployeeTermination {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Builder.Default
    @Column(name = "external_id", unique=true,
                        nullable=false, updatable=false)
    private UUID externalId = UUID.randomUUID();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_document_id", nullable = true)
    private HumanResourcesDocument supportingDocument;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TerminationReason terminationReason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EmployeeTransitionStatus status;

    @Column(nullable = false)
    private LocalDate proposedExitDate;

    @Column(length = 1000)
    private String description;

    private String resolvedBy;

    private OffsetDateTime resolvedAt;

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
