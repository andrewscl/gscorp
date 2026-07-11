package com.gscorp.dv1.schedulers;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.gscorp.dv1.operations.shifts.application.ShiftService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ShiftAutomationScheduler {

    private ShiftService shiftService;

    @Scheduled(cron = "0 0 0 * * ?")
    public void runDailyShiftProjection() {

        log.info("Iniciando tarea programada: Proyección de turnos a 30 días...");

        shiftService.processApprovedShiftRequests();

        log.info("Tarea programada finalizada con exito.");

    }

}
