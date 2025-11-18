package com.gscorp.dv1.shiftrequests.infrastructure;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.gscorp.dv1.enums.ShiftRequestStatus;
import com.gscorp.dv1.enums.ShiftRequestType;
import com.gscorp.dv1.sites.infrastructure.Site;

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
@Table(name = "shift_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShiftRequest {

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

    private Long clientAccountId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private ShiftRequestType type;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    private ShiftRequestStatus status; // PENDING, CONFIRMED, CANCELLED

    @Column(name = "description", length = 1024)
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @OneToMany(mappedBy = "shiftRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ShiftRequestSchedule> schedules = new ArrayList<>();
}
