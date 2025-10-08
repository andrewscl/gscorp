package com.gscorp.dv1.patrol.application;

import java.time.LocalDate;
import java.time.ZoneId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gscorp.dv1.patrol.infrastructure.PatrolRunRepo;

import lombok.RequiredArgsConstructor;

    @Service
    @RequiredArgsConstructor
    public class PatrolService {
        
    private final PatrolRunRepo runs;

    @Transactional(readOnly = true)
    public Double compliance(Long clientId, LocalDate from, LocalDate to, ZoneId zone) {
        var fromTs = from.atStartOfDay(zone).toOffsetDateTime();
        var toTs   = to.plusDays(1).atStartOfDay(zone).minusNanos(1).toOffsetDateTime();
        return runs.compliance(clientId, fromTs, toTs); // puede devolver null si no hay expected
    }

    @Transactional(readOnly = true)
    public PatrolRunRepo.HitsSum hitsSum(Long clientId, LocalDate from, LocalDate to, ZoneId zone) {
        var fromTs = from.atStartOfDay(zone).toOffsetDateTime();
        var toTs   = to.plusDays(1).atStartOfDay(zone).minusNanos(1).toOffsetDateTime();
        return runs.hitsSum(clientId, fromTs, toTs);
    }

}
