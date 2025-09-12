package com.gscorp.dv1.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.web.reactive.function.client.WebClient;

import io.netty.channel.ChannelOption;
import reactor.netty.http.client.HttpClient;

@Configuration
@EnableRetry
public class WebClientConfig {
    
    @Bean
    WebClient mercadoPublicoWebClient (
        @Value("${gscorp.mp.base-url}") String baseUrl,
        @Value("${gscorp.mp.connect-timeout-ms}") int connectTimeout,
        @Value("${gscorp.mp.read-timeout-ms}") int readTimeout
    )   {
        
        //Configuracion del Http
        HttpClient httpClient = HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout)
            .responseTimeout(Duration.ofMillis(readTimeout));

        return WebClient.builder()
            .baseUrl(baseUrl)
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .build();
    }
}