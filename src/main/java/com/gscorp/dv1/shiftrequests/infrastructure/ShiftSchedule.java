package com.gscorp.dv1.shiftrequests.infrastructure;

import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
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
@Table(name = "shift_schedules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShiftSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Ej: "Lunes", "Jueves"
    @Column(name = "day_from", nullable = false, length = 12)
    private String dayFrom;

    @Column(name = "day_to", nullable = false, length = 12)
    private String dayTo;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "lunch_time")
    private LocalTime lunchTime;

    // Relación de pertenencia al ShiftRequest
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shift_request_id")
    private ShiftRequest shiftRequest;

}
