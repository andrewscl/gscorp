package com.gscorp.dv1.shiftrequests.application;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.gscorp.dv1.enums.RequestType;
import com.gscorp.dv1.shiftrequests.infrastructure.ShiftRequest;
import com.gscorp.dv1.shiftrequests.infrastructure.ShiftRequestRepository;
import com.gscorp.dv1.shiftrequests.infrastructure.ShiftSchedule;
import com.gscorp.dv1.shiftrequests.web.dto.CreateShiftRequestRequest;
import com.gscorp.dv1.sites.infrastructure.SiteRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ShiftRequestServiceImpl implements ShiftRequestService {

    private final ShiftRequestRepository shiftRequestRepository;
    private final SiteRepository siteRepository;
    
    @Override
    public List<ShiftRequest> findAll() {
        return shiftRequestRepository.findAll();
    }

    @Override
    public Optional<ShiftRequest> findById(Long id) {
        return shiftRequestRepository.findById(id);
    }

    @Override
    public ShiftRequest create(CreateShiftRequestRequest req) {

        //Obtener sitio y tipo
        String requestType = req.type();
        Long siteId = req.siteId();

        //Buscar ultimo código generado en este sitio
        String prefix = 
            requestType.equals("FIXED") ? "TF" :
            requestType.equals("SPORADIC") ? "TS": "TX";

            //buscar el ultimo código generado en este sitio
        String lastCode = shiftRequestRepository.findLastCodeBySiteIdAndPrefix(siteId, prefix);

        int nextCorrelative = 1;
        if (lastCode != null && lastCode.startsWith(prefix)) {
            //Extraer el numero correlativo y aumentar en 1
            try{
                nextCorrelative = Integer.parseInt(lastCode.substring(2)) + 1;
            } catch (NumberFormatException e) {
                nextCorrelative = 1; //Si hay error, iniciar en 1
            }
        }

        //Generar el nuevo codigo
        String newCode = prefix + String.format("%04d", nextCorrelative);
        
        //Construir ShiftRequest
        ShiftRequest shiftRequest = ShiftRequest.builder()
            .code(newCode)
            .site(siteRepository.findById(siteId).
                        orElseThrow(() -> new IllegalArgumentException("Site not found")))
            .type(RequestType.valueOf(requestType))
            .clientAccountId(req.accountId())
            .startDate(req.startDate())
            .endDate(req.endDate())
            .status("PENDING")
            .description(req.description())
            .build();

        //Maprear y asociar schedules
        if ( req.schedules() != null && !req.schedules().isEmpty()) {
            List<ShiftSchedule> schedules = req.schedules().stream()
                .map(schedReq -> ShiftSchedule.builder()
                    .dayFrom(schedReq.dayFrom())
                    .dayTo(schedReq.dayTo())
                    .startTime(LocalTime.parse(schedReq.startTime()))
                    .endTime(LocalTime.parse(schedReq.endTime()))
                    .lunchTime(schedReq.lunchTime() != null ? LocalTime.parse(schedReq.lunchTime()) : null)
                    .shiftRequest(shiftRequest) // Asociar al ShiftRequest
                    .build()
                ).toList();
            shiftRequest.setSchedules(schedules);
        }

        return shiftRequestRepository.save(shiftRequest);
    }

    @Override
    public ShiftRequest update(ShiftRequest shiftRequest) {
        return shiftRequestRepository.save(shiftRequest);
    }
}
