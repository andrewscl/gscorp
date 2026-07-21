package com.gscorp.dv1.hr.hrdocs.infrastructure;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.gscorp.dv1.configuration.hrdocuments.infrastructure.HrDocumentType;
import com.gscorp.dv1.hr.employees.infrastructure.Employee;
import com.gscorp.dv1.hr.employeeterminations.infrastructure.EmployeeTermination;

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

@Entity
@Table (name="hr_documents")
@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
@Builder
public class HumanResourcesDocument {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Builder.Default
    @Column(name = "external_id", unique=true,
                        nullable=false, updatable=false)
    private UUID externalId = UUID.randomUUID();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_termination_id", nullable = true) // Nullable porque no todos los docs son de desvinculación
    private EmployeeTermination employeeTermination;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hrdocumenttype_id", nullable = false)
    private HrDocumentType hrDocumentType;

    @Column(nullable = false)
    private String fileUrl;

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
