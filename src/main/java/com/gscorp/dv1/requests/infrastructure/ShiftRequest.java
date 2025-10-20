package com.gscorp.dv1.requests.infrastructure;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.gscorp.dv1.sites.infrastructure.Site;

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
@Table(name = "shift_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShiftRequest {

    public enum RequestType {
        FIXED, SPORADIC
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Readable and sequential code: TR001, TR002, etc.
    @Column(name = "code", unique = true, nullable = false, length = 16)
    private String code;

    // Direct relationship only with Site
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", nullable = false)
    private Site site;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private RequestType type;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    // For FIXED: days of the week (e.g., "MONDAY,WEDNESDAY")
    @Column(name = "week_days")
    private String weekDays;

    // For SPORADIC: specific date and time
    @Column(name = "shift_datetime")
    private LocalDateTime shiftDateTime;

    // Shift start time
    @Column(name = "start_time")
    private LocalTime startTime;

    // Shift end time
    @Column(name = "end_time")
    private LocalTime endTime;

    // Shift lunch time
    @Column(name = "lunch_time")
    private LocalTime lunchTime;

    @Column(name = "status", nullable = false)
    private String status; // PENDING, CONFIRMED, CANCELLED

    @Column(name = "description", length = 1024)
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
