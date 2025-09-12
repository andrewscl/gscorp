package com.gscorp.dv1.api.clients;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.gscorp.dv1.api.dto.LicitationsResponse;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class PublicMarketClient {
    
    private final WebClient mercadoPublicoWebClient;

    @Value("${gscorp.mp.ticket}")
    private String ticket;

    public Mono<LicitationsResponse> getLicitationsToday(){
        return mercadoPublicoWebClient.get()
            .uri (uri -> uri.path("/servicios/v1/publico/licitaciones.json")
            .queryParam("ticket", ticket)
            .build())
        .accept(MediaType.APPLICATION_JSON)
        .retrieve()
        .bodyToMono(LicitationsResponse.class);
    }
    
    public Mono<String> getLicitationsByCodeRaw(String code) {
        return mercadoPublicoWebClient.get()
            .uri (uri -> uri.path("/servicios/v1/publico/licitaciones.json")
                .queryParam("codigo", code)
                .queryParam("ticket", ticket)
                .build())
        .accept(MediaType.APPLICATION_JSON)
        .retrieve()
        .bodyToMono(String.class);
    }
    
}
