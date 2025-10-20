package com.gscorp.dv1.planning.infrastructure;

import java.time.LocalDateTime;

import com.gscorp.dv1.requests.infrastructure.ShiftRequest;
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
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "planned_shifts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlannedShift {

    private ShiftRequest shiftRequest;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Reference to the request that originated this planned shift
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    private ShiftRequest request;

    // Planned date and time for the shift
    @Column(name = "scheduled_datetime", nullable = false)
    private LocalDateTime scheduledDateTime;

    // The user who was assigned to perform the shift
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to_id")
    private User assignedTo;

    // The user who actually performed the shift
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "executed_by_id")
    private User executedBy;

    // Actual start and end times of the shift
    @Column(name = "actual_start_datetime")
    private LocalDateTime actualStartDateTime;

    @Column(name = "actual_end_datetime")
    private LocalDateTime actualEndDateTime;

    // Actual start and end times of lunch
    @Column(name = "actual_start_time_lunch")
    private LocalDateTime actualStartTimeLunch;

    @Column(name = "actual_end_time_lunch")
    private LocalDateTime actualEndTimeLunch;

    // Current status of the shift (PLANNED, COMPLETED, CANCELLED, etc.)
    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "notes", length = 512)
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}