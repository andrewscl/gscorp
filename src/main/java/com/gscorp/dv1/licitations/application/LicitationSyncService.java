package com.gscorp.dv1.licitations.application;

import java.time.Instant;
import java.util.List;

import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gscorp.dv1.licitations.infrastructure.Licitation;
import com.gscorp.dv1.licitations.infrastructure.LicitationRepository;
import com.gscorp.dv1.licitations.infrastructure.mapper.LicitationDtoMapper;
import com.gscorp.dv1.licitations.web.dto.LicitationDto;
import com.gscorp.dv1.licitations.web.dto.LicitationsResponseDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class LicitationSyncService {
    
    private final LicitationService licitationService;
    private final LicitationRepository licitationRepository;

    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))
    @Transactional
    public int syncTodayLicitations(){

        LicitationsResponseDto resp = licitationService.getLicitationsToday().block();
        if(resp == null || resp.listado() == null) return 0;

        List<LicitationDto> lista = resp.listado();
        int count = 0;

        for(LicitationDto dto : lista) {
            if(dto.externalCode() == null) continue;

            Licitation licitation = licitationRepository.findById(dto.externalCode()).orElse(null);
            licitation = LicitationDtoMapper.toEntity(dto, licitation);
            licitation.setLastSync(Instant.now());

            licitationRepository.save(licitation);
            count++;
        }
        log.info("MP: {} licitaciones sincronizadas (hoy)", count);
        return count;
    }

}
