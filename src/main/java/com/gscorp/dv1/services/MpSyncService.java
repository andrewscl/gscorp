package com.gscorp.dv1.services;

import java.time.Instant;
import java.util.List;

import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gscorp.dv1.api.clients.PublicMarketClient;
import com.gscorp.dv1.api.dto.LicitationDTO;
import com.gscorp.dv1.api.dto.LicitationsResponse;
import com.gscorp.dv1.entities.Licitation;
import com.gscorp.dv1.repositories.LicitationRepository;
import com.gscorp.dv1.services.mapper.LicitationMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MpSyncService {
    
    private final PublicMarketClient client;
    private final LicitationRepository repo;

    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))
    @Transactional
    public int syncTodayLicitations(){

        LicitationsResponse resp = client.getLicitationsToday().block();
        if(resp == null || resp.getListado() == null) return 0;

        List<LicitationDTO> lista = resp.getListado();
        int count = 0;

        for(LicitationDTO dto : lista) {
            if(dto.getExternalCode() == null) continue;

            Licitation licitation = repo.findById(dto.getExternalCode()).orElse(null);
            licitation = LicitationMapper.toEntity(dto, licitation);
            licitation.setLastSync(Instant.now());

            repo.save(licitation);
            count++;
        }
        log.info("MP: {} licitaciones sincronizadas (hoy)", count);
        return count;
    }

}
