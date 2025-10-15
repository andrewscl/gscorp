package com.gscorp.dv1.licitations.application;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.gscorp.dv1.licitations.web.dto.LicitationsResponseDto;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class LicitationService {
    
    private final WebClient mercadoPublicoWebClient;

    @Value("${gscorp.mp.ticket}")
    private String ticket;

    /**
     * Consulta licitaciones desde fechaDesde hasta fechaHasta (formato yyyy-MM-dd HH:mm:ss).
     * Se recomienda usar LocalDateTime y convertir a String con el formato adecuado.
     */
    public Mono<LicitationsResponseDto> getLicitationsByDate(LocalDateTime fechaDesde, LocalDateTime fechaHasta) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String desdeStr = fechaDesde.format(formatter);
        String hastaStr = fechaHasta.format(formatter);

        return mercadoPublicoWebClient.get()
            .uri(uri -> uri.path("/servicios/v1/publico/licitaciones.json")
                .queryParam("ticket", ticket)
                .queryParam("fechaDesde", desdeStr)
                .queryParam("fechaHasta", hastaStr)
                .build())
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .bodyToMono(LicitationsResponseDto.class);
    }

    /**
     * Consulta licitaciones publicadas hoy (00:00:00 a 23:59:59)
     */
    public Mono<LicitationsResponseDto> getLicitationsToday() {
        LocalDateTime inicio = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime fin = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59).withNano(999999999);
        return getLicitationsByDate(inicio, fin);
    }

}
